HTML5 and CSS3 based File Uploader
==================================


## About

Grails 3 is based on Spring-Boot and plugins written pre-Grails 3 have to be "re-structured" or re-configured for Grails 3.

This is an upgrade to grails 3.x of the excellent plugin written by Dustin Clark [here](https://github.com/dustindclark/grails-uploadr/blob/master/README.md)

## Usage
In the project that you would like to use the uploadr plugin, include the following in its build.gradle.

<pre>
buildscript {
...
    dependencies {
        classpath 'com.bertramlabs.plugins:asset-pipeline-gradle:2.5.0'
        ...
    }
}
</pre>

and

<pre>
dependencies {
 ...
    compile "com.nayidisha.grails.uploadr:grails3-uploadr:3.0"
...
}
</pre>

Then in a gsp where the uploadr needs to be installed:

    <!DOCTYPE HTML>
    <html>
    <head>
        ...
        <asset:javascript src="uploadr.manifest.js"/>
        <asset:javascript src="uploadr.demo.manifest.js"/>
        <asset:stylesheet href="uploadr.manifest.css"/>
        <asset:stylesheet href="uploadr.demo.manifest.css"/>
        ...
    </head>
    <body>
        ...
        <uploadr:demo/>
        ...
    </body>
    </html>

## Images

When your gsp is configured with a tag like so:

    <uploadr:add name="aFileToUpload.png" path="/somewhereOnYourFS" maxSize="52428800" />

Here is how a single file upload looks:

![uploadImage](uploadbefore.png)

and after upload...

![uploadImage](upload.png)

## ToDo

- i18n is still not working correctly, so all messages have been hardcoded in English.

