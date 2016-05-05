/**
 *  Uploadr, a multi-file uploader plugin
 *  Copyright (C) 2011 Jeroen Wesbeek
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hungry.wombat

import grails.converters.JSON

import org.springframework.util.FileCopyUtils

class UploadController {

    def index() {
        redirect(uri: "/")
    }

    def warning() {
        render(plugin: 'uploadr', template: '/upload/warning')
    }

    def handle() {
        String contentType = request.getHeader("Content-Type")
        String fileName = URLDecoder.decode(request.getHeader('X-File-Name'), 'UTF-8')
        long fileSize = (request.getHeader('X-File-Size') != "undefined") ? request.getHeader('X-File-Size') as long : 0
        String name = URLDecoder.decode(request.getHeader('X-Uploadr-Name'), 'UTF-8')
        def info = session.getAttribute('uploadr')
        def myInfo = (name && info && info.containsKey(name)) ? info[name] : [:]
        String savePath = myInfo.path ?: "/tmp"
        def dir = new File(savePath)
        def file = new File(savePath, fileName)
        def namePart = ""
        def extension = ""
        def testName = ""
        int status = 0
        def statusText = ""

        response.contentType = 'application/json'

        // update lastUsed stamp in session
        if (name && info && info.containsKey(name)) {
            session.uploadr[name].lastUsed = new Date()
            session.uploadr[name].lastAction = "upload"
        }

        if (!dir.exists()) {
            try {
                dir.mkdirs()
            } catch (e) {
                response.sendError(500, "could not create upload path ${savePath}")
                render([written: false, fileName: file.name] as JSON)
                return false
            }
        }

        def freeSpace = dir.getUsableSpace()
        if (fileSize > freeSpace) {
            response.sendError(500, "cannot store '${fileName}' (${fileSize} bytes), only ${freeSpace} bytes of free space left on device")
            render([written: false, fileName: file.name] as JSON)
            return false
        }

        if (!dir.canWrite()) {
            if (!dir.setWritable(true)) {
                response.sendError(500, "'${savePath}' is not writable, and unable to change rights")
                render([written: false, fileName: file.name] as JSON)
                return false
            }
        }

        // make sure the file name is unique
        int dot = fileName.lastIndexOf(".")
        namePart = (dot) ? fileName[0..dot - 1] : fileName
        extension = (dot) ? fileName[dot + 1..fileName.length() - 1] : ""
        int testIterator = 1
        while (file.exists()) {
            testName = "${namePart}-${testIterator}.${extension}"
            file = new File(savePath, testName)
            testIterator++
        }

        // handle file upload
        try {
            FileCopyUtils.copy(request.inputStream, new FileOutputStream(file))
            status = 200
            statusText = "'${file.name}' upload successful!"
        } catch (e) {
            status = 500
            statusText = e.message
        }

        // make sure the file was properly written
        if (status == 200 && fileSize > file.size()) {
            // whoops, looks like the transfer was aborted!
            status = 500
            statusText = "'${file.name}' transfer incomplete, received ${file.size()} of ${fileSize} bytes"
        }

        // got an error of some sorts?
        if (status != 200) {
            // then -try to- delete the file
            try {
                file.delete()
            } catch (ignored) {
            }
        }

        // render json response
        response.setStatus(status)
        render([written: (status == 200), fileName: file.name, status: status, statusText: statusText] as JSON)
    }

    def delete() {
        def fileName = URLDecoder.decode(request.getHeader('X-File-Name'), 'UTF-8')
        def name = URLDecoder.decode(request.getHeader('X-Uploadr-Name'), 'UTF-8')
        def info = session.getAttribute('uploadr')
        def savePath = (name && info && info.get(name) && info.get(name).path) ? info.get(name).path : '/tmp'
        def file = new File(savePath, fileName)

        // update lastUsed stamp in session
        if (name && info && info.containsKey(name)) {
            session.uploadr[name].lastUsed = new Date()
            session.uploadr[name].lastAction = "delete"
        }

        if (file.exists()) {
            try {
                // delete file
                file.delete()

                response.sendError(200, "OK, deleted '${fileName}'")
            } catch (e) {
                response.sendError(500, "could not delete '${fileName}' (${e.getMessage()}")
            }
        } else {
            response.sendError(200, "OK, '${fileName}' did not -yet- exist")
        }
    }

    def download() {
        def fileName = URLDecoder.decode(params.get('file'), 'UTF-8')
        def name = URLDecoder.decode(params.get('uploadr'), 'UTF-8')
        def info = session.getAttribute('uploadr')
        def savePath = (name && info && info.get(name) && info.get(name).path) ? info.get(name).path : '/tmp'
        def file = new File(savePath, fileName)

        // path traversal protection
        if (file && file.exists() && fileName =~ /\\|\//) {
            response.sendError(400, "could not download '${fileName}': access denied")
            return false
        } else if (file && file.exists() && file.canRead()) {
            // update lastUsed stamp in session
            if (name && info && info.containsKey(name)) {
                session.uploadr[name].lastUsed = new Date()
                session.uploadr[name].lastAction = "download"
            }

            // download file
            response.setStatus(200)
            response.setContentType("application/octet-stream")
            response.setContentLength(file.size() as int)

            // browsers do not handle RFC5987 properly, so Safari will be unable to decode the unicode filename
            // @see http://greenbytes.de/tech/tc2231/
            response.setHeader("Content-Disposition", "attachment; filename=${URLEncoder.encode(fileName, 'ISO-8859-1')}; filename*= UTF-8''${URLEncoder.encode(fileName, 'UTF-8')}")

            // handle file upload
            try {
                FileCopyUtils.copy(new FileInputStream(file), response.outputStream)
            } catch (e) {
                log.error "download failed! $e.message"
            }
        } else if (file?.exists() && !file.canRead()) {
            // file not readable
            response.sendError(400, "could not download '${fileName}': access denied")
            return false
        } else {
            // file not found
            response.sendError(400, "could not download '${fileName}': file not found")
            return false
        }

        // return false as we do not have a view
        return false
    }
}
