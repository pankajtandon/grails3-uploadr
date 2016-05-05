HTML5 and CSS3 based File Uploader
==================================


## About

Grails 3 is a Spring-Boot based architecture and plugins written pre Grails 3 have to be re-written for Grails 3.

This is an upgrade to grails 3.x of the excellent plugin written by Dustin Clark [here](https://github.com/dustindclark/grails-uploadr/blob/master/README.md)

## Usage

This plugin is still not on bintray.
In the meanwhile, download the project and issue a
<pre>
grails install
</pre>

Then in the project that you would like to use the uploadr, include the following in build.gradle.

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

In the gsp where the uploadr needs to be installed:

<pre>

&lt;%@page expressionCodec="raw" %&gt;
&lt;!DOCTYPE HTML&gt;
&lt;html&gt;
&lt;head&gt;
    ...
    &lt;asset:javascript src="uploadr.manifest.js"/&gt;
    &lt;asset:javascript src="uploadr.demo.manifest.js"/&gt;
    &lt;asset:stylesheet href="uploadr.manifest.css"/&gt;
    &lt;asset:stylesheet href="uploadr.demo.manifest.css"/&gt;
    &lt;asset:stylesheet href="bootstrap.manifest.css"/&gt;
    ...
&lt;/head&gt;
&lt;body&gt;
    ...
    &lt;uploadr:demo/&gt;
    ...
&lt;/body&gt;
&lt;/html&gt;


</pre>


## ToDo

- i18n is still not working correctly, so all messages have been hardcoded in English.
 