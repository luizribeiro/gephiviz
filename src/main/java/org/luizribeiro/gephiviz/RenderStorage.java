package org.luizribeiro.gephiviz;

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

public class RenderStorage {

    protected S3Service s3Service;
    protected S3Bucket s3Bucket;
    protected String bucketName;

    public RenderStorage() throws S3ServiceException {
        AWSCredentials awsCredentials = new AWSCredentials(
                Settings.getAwsAccessKey(), Settings.getAwsSecret());
        s3Service = new RestS3Service(awsCredentials);
        bucketName = "gephiviz-" + Settings.getApiKey();
        s3Bucket = s3Service.getOrCreateBucket(bucketName);
    }

    public void storeImage(RenderedImage image, String filename)
            throws IOException, S3ServiceException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        storeByteArray(os.toByteArray(), filename, "image/png");
    }

    public void storeByteArray(byte buffer[], String filename, String contentType)
            throws IOException, S3ServiceException {
        S3Object s3Object = new S3Object(filename);
        s3Object.setDataInputStream(new ByteArrayInputStream(buffer));
        s3Object.setContentType(contentType);
        s3Object.setContentLength(buffer.length);

        // upload object
        s3Service.putObject(s3Bucket, s3Object);
    }

    public S3Object retrieveS3Object(String filename) throws S3ServiceException {
        return s3Service.getObject(s3Bucket, filename);
    }

    public boolean hasObject(String filename) throws ServiceException {
        return s3Service.isObjectInBucket(bucketName, filename);
    }
}
