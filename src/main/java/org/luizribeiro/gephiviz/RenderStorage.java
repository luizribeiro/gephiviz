package org.luizribeiro.gephiviz;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

public class RenderStorage {

    S3Service s3Service;
    S3Bucket s3Bucket;

    public RenderStorage() throws S3ServiceException {
        AWSCredentials awsCredentials = new AWSCredentials(
                Settings.getAwsAccessKey(), Settings.getAwsSecret());
        s3Service = new RestS3Service(awsCredentials);
        s3Bucket = s3Service.getOrCreateBucket("gephiviz-" + Settings.getApiKey());
    }

    public void storeRenderedTile(byte buffer[], String uid)
            throws IOException, S3ServiceException {
        S3Object s3Object = new S3Object(uid + ".png");
        s3Object.setDataInputStream(new ByteArrayInputStream(buffer));
        s3Object.setContentType("image/png");
        s3Object.setContentLength(buffer.length);

        // make this object public
        s3Object.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);

        // upload object
        s3Service.putObject(s3Bucket, s3Object);
    }
}
