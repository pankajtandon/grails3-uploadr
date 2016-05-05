package hungry.wombat

import grails.test.mixin.TestFor
import org.springframework.http.HttpStatus
import spock.lang.Specification

@TestFor(UploadController)
class UploadControllerSpec extends Specification {

    static final String FILE_CONTENT = "Unit test upload."

    private Map getUploadrInfo() {
        return ['uploadr': ['path': "${System.getProperty("java.io.tmpdir")}/uploadr"]]
    }

    void "test upload"() {
        when:
        session.uploadr = getUploadrInfo()
        byte[] fileContent = FILE_CONTENT.getBytes()
        request.content = fileContent
        request.addHeader("Content-Type", "text/plain")
        request.addHeader("X-Uploadr-Name", "uploadr")
        request.addHeader('X-File-Name', 'unit-test.txt')
        request.addHeader('X-File-Size', fileContent.length)

        controller.handle()

        then:
        response.status == HttpStatus.OK.value()
        response.json.written == true
        response.json.fileName == 'unit-test.txt'
        response.json.statusText == "'unit-test.txt' upload successful!"
    }

    void "test download"() {
        when:
        session.uploadr = getUploadrInfo()
        params.file = 'unit-test.txt'
        params.uploadr = 'uploadr'

        controller.download()

        then:
        response.status == HttpStatus.OK.value()
        response.contentLength == FILE_CONTENT.bytes.length
        response.text == FILE_CONTENT
    }

    void "test delete"() {
        when:
        session.uploadr = getUploadrInfo()
        request.addHeader("X-Uploadr-Name", "uploadr")
        request.addHeader('X-File-Name', 'unit-test.txt')

        controller.delete()

        then:
        response.status == HttpStatus.OK.value()
        response.errorMessage == "OK, deleted 'unit-test.txt'"
    }
}
