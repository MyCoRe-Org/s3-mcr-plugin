# MinIO example

Runs [MinIO](https://min.io/) as S3 compatible store to test the plugin locally.

The official `minio/minio` image is no longer available on Docker Hub, so this example uses the
[Chainguard build](https://images.chainguard.dev/directory/image/minio/overview).

## Start

The image runs MinIO as non-root user (UID 65532), so the data directory must be writable for this user:

```sh
cd examples/minio
mkdir -p data && sudo chown 65532:65532 data
docker compose up -d
```

- S3 API: http://localhost:9000
- Console: http://localhost:9001 (user `admin`, password `changeme123`,
  can be changed with `MINIO_ROOT_USER` and `MINIO_ROOT_PASSWORD`)

Create a bucket and upload some files in the console.

## Connect MyCoRe

If MyCoRe (e.g. MIR) also runs in Docker, connect its container to the network `s3net`:

```sh
docker network connect s3net <mir-container>
```

or add the network to its compose file:

```yaml
services:
  mir:
    networks:
      - default
      - s3net

networks:
  s3net:
    external: true
```

Enable the download proxy, since browsers cannot reach the MinIO container directly:

```
MCR.ExternalStore.ProxyServlet.Disabled=false
```

## Link a bucket

Use the following settings in the file browser:

| Setting                 | Value                                               |
|-------------------------|-----------------------------------------------------|
| Protocol                | `http`                                              |
| Endpoint                | `minio:9000` (MyCoRe in Docker) or `localhost:9000` |
| Bucket                  | name of the bucket                                  |
| Access Key / Secret Key | MinIO user and password                             |
| Path Style Access       | enabled                                             |
| Download Proxy          | enabled                                             |
