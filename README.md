# Plugin
This plugin provides a additional file view to view buckets stored in S3. It also adds some rest endpoint and makes it possible to link buckets to MODS objects.

## Configuration

The plugin comes preconfigured with the following properties. 
```
MCR.FS.Impl.S3=org.mycore.filesystem.s3.XMLS3BucketProvider
MCR.FS.Impl.S3.Key=bucket-crypt

MCR.Crypt.Cipher.bucket-crypt.class=org.mycore.crypt.MCRAESCipher
MCR.Crypt.Cipher.bucket-crypt.KeyFile=%MCR.datadir%/bucket.key
```
### Create key file
You need to create the bucket-crypt key with the CLI command:
```
generate keyfile for cipher bucket-crypt
```
### Embed
If you are using a MIR you will get a preconfigured metadata-page id which you can insert e.g.
```
MIR.Layout.Start=mir-abstract-badges,mir-workflow,mir-abstract-title,mir-abstract-plus,toc,mir-file-upload,mir-viewer,mir-player,mir-extended-file-browser,mir-collapse-files,mir-metadata
```
If not you can take a look at the file **src/main/resources/xsl/metadata/extended-file-browser.xsl** to see how it is done.

### ACL
The user which read or write the encrypted data from the object need to have the permission for that.
The users also need the right to read and write from the rest API.

| Objekt ID                 | Zugriffsrecht | Regel                    |
|---------------------------|---------------|--------------------------|
| crypt:cipher:bucket-crypt | crypt         | editor and administrator |
| crypt:cipher:bucket-crypt | decrypt       | always allowed           |
| restapi:/fs/              | write         | editor and administrator |
| restapi:/fs/              | read          | always allowed           |

### Derivate types
You need to add derivate_types:extension to the derivate_types classification.

## How is the Bucket stored?
The Bucket Data is stored as derivate with derivate_type:extension.
```
<folder-extension-bind class="org.mycore.filesystem.s3.XMLS3BucketProvider" encrypted="false">
    <XMLS3Bucket>
        <endpoint>s3archive.gbv.de:9003</endpoint>
        <accessKey>05698686438511ec872adfc2556acb1e</accessKey>
        <secretKey>xxxxxxxxxxxxxxxxxxxxxxxxxx</secretKey>
        <protocol>http</protocol>
        <bucket>testbucket01</bucket>
    </XMLS3Bucket>
</folder-extension-bind>


<folder-extension-bind class="org.mycore.filesystem.s3.XMLS3BucketProvider" encrypted="true" key="bucket-crypt">
KDaecE1SXn851uXbEi5DNah+fiaNyG7LAPsI3R22S0pSMYAnJoqpjOj9YY2H6RakiUaJwnOjpwheKa+TaNzYl3Ci6UcgtHy/CdiM4rgz9cWCSWjtGPPeUL2MU9CsaF8dEL4gCPOO10lBOyZzN11gVDUkMxMJGsqcy+WUcqLmJvPaudp/PSt5bMrBOijzqeuhjS2kz+2IF1p/wQfTM4TL7WlSlJEgRBJJDfIh3RBtbWxJ8F0g3iZqumzxX3ZL+dBST2F+lIZmwKDZL0taBosFIPH/rH4MbD1JcMM2iwKol76mNPIiuMDZcPZERrnz91wDlQqqI9STNOfMieTDbyH8wA==
</folder-extension-bind>
```
## How can I test local?
You can use minio for testing s3 with docker and docker-compose.
Copy this to a docker-compose.yml and run `docker-compose up` to start a demo s3 server.
```
version: '3.3'
services:
    minio:
        ports:
            - '9000:9000'
            - '9001:9001'
        environment:
            - MINIO_ROOT_USER=admin
            - MINIO_ROOT_PASSWORD=alleswirdgut
        volumes:
            - './data:/data'
        image: minio/minio
        command: server /data --console-address :9001

```

The login for the admin user is stored in the environment variables **MINIO_ROOT_USER** and **MINIO_ROOT_PASSWORD**.