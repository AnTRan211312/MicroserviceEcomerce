package com.ecomerce.productservice.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@ConditionalOnExpression("!'${aws.access-key:}'.isEmpty() && !'${aws.secret-key:}'.isEmpty()")
public class S3Config {

    @Value("${aws.access-key:}")
    private String accesskey;
    @Value("${aws.secret-key:}")
    private String secretkey;
    @Value("${aws.region:ap-southeast-1}")
    private String region;
    @Value("${aws.s3.bucket-name:}")
    private String awsBucketName;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accesskey, secretkey);
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accesskey, secretkey);
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Bean
    public String awsBucketName() {
        return awsBucketName;
    }

    @Bean
    public String awsRegion() {
        return region;
    }


}
