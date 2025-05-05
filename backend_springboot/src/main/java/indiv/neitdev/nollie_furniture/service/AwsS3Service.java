package indiv.neitdev.nollie_furniture.service;

import indiv.neitdev.nollie_furniture.exception.AppException;
import indiv.neitdev.nollie_furniture.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URI;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AwsS3Service {

    @NonFinal
    @Value("${BUCKET_NAME}")
    String bucketName;

    S3Client s3Client;

    Region region = Region.AP_SOUTHEAST_2;


    public String saveImageToS3(MultipartFile photo) {
        try {
            String filename = photo.getOriginalFilename();
            if (filename == null || filename.isEmpty()) {
                throw new IllegalArgumentException("Filename is empty");
            }

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .contentType(photo.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(photo.getInputStream(), photo.getSize()));

            return "https://" + bucketName + ".s3." + region.id() + ".amazonaws.com/" + filename;

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.FAIL_UPLOAD_TO_S3);
        }
    }

    public void deleteImageFromS3(String filename) {
        try {

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build();

            s3Client.deleteObject(request);
            log.info("Deleted file {} from bucket {}", filename, bucketName);

        } catch (S3Exception e) {
            log.error("Error deleting file from S3: {}", e.awsErrorDetails().errorMessage());
            throw new AppException(ErrorCode.FAIL_UPLOAD_TO_S3);
        }
    }
}
