# Plugin
This plugin provides an additional file view to view buckets stored in an external store.
It also adds some rest endpoint and makes it possible to link buckets to MODS objects.
An external store provides information about files and also allows to download them.
To ensure integrity, information for all files in the external store is stored in a derivate.
After integration, it is no longer necessary to communicate with the store for file information, as the information is provided via a local index.
It is currently possible to integrate an s3 buckets as external stores.
In addition, archives can be resolved to provide information about the files. It may also be possible to download files directly from archives.

## Configuration

The plugin comes preconfigured with the following properties. 
```
MCR.Crypt.Cipher.external-store-settings.class=org.mycore.crypt.MCRAESCipher
MCR.Crypt.Cipher.external-store-settings.KeyFile=%MCR.datadir%/external_store_settings.key
MCR.Crypt.Cipher.external-store-settings.EnableACL=false

MCR.ExternalStore.Service.StoreProviderSettings.Cipher=external-store-settings
MCR.ExternalStore.Service.ResolveArchives=true

MCR.ExternalStore.s3.Provider.Class=org.mycore.externalstore.s3.MCRExternalStoreS3Provider

MCR.ExternalStore.MaxDownloadSize=1000000

MCR.ExternalStore.InfoIndex.Class=org.mycore.externalstore.index.db.MCRExternalStoreDbStoreInfoIndex

MCR.ExternalStore.ArchiveResolver.zip.Download=true
MCR.ExternalStore.ArchiveResolver.tar.Download=false

MCR.ExternalStore.ProxyServlet.Disabled=true
MCR.ExternalStore.ProxyServlet.Url=%MCR.baseurl%/esp
```
### Create key file
You need to create the external-store-settings key with the CLI command:
```
generate keyfile for cipher external-store-settings
```
### Embed
If you are using a MIR you will get a preconfigured metadata-page id which you can insert e.g.
```
MIR.Layout.Start=mir-abstract-badges,mir-workflow,mir-abstract-title,mir-abstract-plus,toc,mir-file-upload,mir-viewer,mir-player,mir-extended-file-browser,mir-collapse-files,mir-metadata
```
If not you can take a look at the file **src/main/resources/xsl/metadata/extended-file-browser.xsl** to see how it is done.


### Derivate types
You need to add `derivate_types:external_store_<type>` to the derivate_types classification, for s3 buckets it is `derivate_types:external_store_s3`.

## How are settings and information saved?
The relevant data is stored in associated derivate.
Sensitive information, such as settings for communication with the provider of the store, is stored in encrypted form in provider_settings.json.
Information about the files is stored in files.json.
Information about archives is stored separately in archives.json because it depends on the configuration of the resolvers.

## Download
Before a download, the integrity is checked and a download URL is generated.
In the case of S3, this is a presigned url.
It is possible that the provider endpoint cannot be reached publicly.
In this case, a proxy can be activated when creating a store.
A proxy servlet is available for this purpose.
Download urls are adapted accordingly and routed through the servlet.
Alternatively, an individual proxy url can be specified so that an independent proxy can also be used.

### Activate proxy servlet
The proxy servlet is deactivated by default and can be activated as follows.
Alternatively, the path can also be adapted.
```
MCR.ExternalStore.ProxyServlet.Disabled=false
MCR.ExternalStore.ProxyServlet.Url=%MCR.baseurl%/esp
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
