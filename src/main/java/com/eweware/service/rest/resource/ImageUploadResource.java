package com.eweware.service.rest.resource;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.eweware.service.base.AWSUtilities;
import com.eweware.service.base.error.*;
import com.eweware.service.base.mgr.SystemManager;
import com.eweware.service.base.store.StoreManager;
import com.eweware.service.base.store.dao.BlahDAO;
import com.eweware.service.base.store.dao.MediaDAO;
import com.eweware.service.base.store.dao.type.DAOUpdateType;
import com.eweware.service.base.store.dao.type.MediaReferendType;
import com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import com.eweware.service.mgr.BlahManager;
import com.eweware.service.mgr.MediaManager;
import com.eweware.service.mgr.UserManager;
import com.eweware.service.rest.RestUtilities;
import com.eweware.service.rest.session.BlahguaSession;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.Info;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Image upload API methods.</p>
 * <div>Note that some methods require authentication (previous login) to be accessed.</div>
 * TODO Need to create jersey-multipart-config.properties and add the following:  bufferThreshold = 128000
 *
 * @author rk@post.harvard.edu
 *         Date: 12/14/12 Time: 3:44 PM
 */
@Path("/images")
public class ImageUploadResource {

    private static final Logger logger = Logger.getLogger(ImageUploadResource.class.getName());

    /**
     * These paths should be part of the config
     */
    private static final String localOriginalImagePath = "/app/blahguarest/images/original/";   // TODO add to config
    private static final String localFormattedImagePath = "/app/blahguarest/images/image/";     // TODO add to config
    private static final String UPLOAD_IMAGE_OPERATION = "uploadImage";
    public static final String canonicalImageFileFormat = ".jpg";

    private static final java.util.Map<String, Integer> supportedUploadFormats = new HashMap<String, Integer>();
    static {
        supportedUploadFormats.put(".jpg", 1);
        supportedUploadFormats.put(".JPG", 1);
        supportedUploadFormats.put(".jpeg", 1);
        supportedUploadFormats.put(".JPEG", 1);
        supportedUploadFormats.put(".png", 1);
        supportedUploadFormats.put(".PNG", 1);
        supportedUploadFormats.put(".gif", 1);
        supportedUploadFormats.put(".GIF", 1);
        supportedUploadFormats.put(".TIF", 1);
        supportedUploadFormats.put(".TIFF", 1);
        supportedUploadFormats.put(".tif", 1);
        supportedUploadFormats.put(".tiff", 1);
    }

    /**
     * Resolution for stored images
     */
    private static final int DEFAULT_IMAGE_DENSITY = 96;

    /**
     * We create a set of versions of the uploaded file
     * in JPG format, each version has a different spec.
     */
    private enum FileTypeSpec {
        A(128, 128, TypeSpecMode.FIXED), // scale & crop to 128x128
        B(256, 256, TypeSpecMode.FIXED), // scale & crop to 256x256
        C(512, 512, TypeSpecMode.FIXED), // scale & crop to 512x512
        D(640, null, TypeSpecMode.WIDTH_DOMINANT); // scale to 768x?

        private final Integer width;
        private final Integer height;
        private final TypeSpecMode mode;

        FileTypeSpec(Integer width, Integer height, TypeSpecMode mode) {
            this.width = width;
            this.height = height;
            this.mode = mode;
        }
    }

    private enum TypeSpecMode {
        FIXED, // Scale so that width and height are preserved
        WIDTH_DOMINANT, // Scale to preserve width
        HEIGHT_DOMINANT // Scale to preserve height
    }

    private StoreManager storeManager;
    private SystemManager systemManager;
    private MediaManager mediaManager;
    private UserManager userManager;
    private BlahManager blahManager;

    /**
     * <p>Not really in use...</p>
     * <div><b>METHOD:</b> </div>
     * <div><b>URL:</b> </div>
     *
     * @param req
     * @return
     */
    @OPTIONS
    @Path("/upload")
    public Response xcorUploadOptions(@Context HttpServletRequest req) {
        Response.ResponseBuilder response = Response.status(200)
                .header("Access-Control-Allow-Origin", "*")    // XXX should limit to the Webapp servers
                .header("Access-Control-Allow-Methods", "POST");
        final String acrh = req.getHeader("Access-Control-Request-Headers");
        if (acrh != null && acrh.length() != 0) {
            response = response.header("Access-Control-Request-Headers", acrh);
        }
        return response.build();
    }

    /**
     * <p>Use this method to upload an image to be related to an object, such as
     * a blah, comment, etc. Returns the media id of the object.</p>
     * <p></p>One may also upload an image that is not yet associated
     * with an object.</p>
     *
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> images/upload</div>
     *
     * @param objectType   Required. The object type. This specifies whether the image is associated
     *                  with a specific type of object. 'B' means a Blah image, 'C' means a comment image,
     *                  'U' means a user image, and 'X' means that the image is not yet
     *                  associated with anything. If the objectType is other than 'X',
     *                  then the id of the object is required.
     * @param objectId  Required only if the objectType is not 'X'.
     *                  This is the object id (e.g., the blah id if the object type is 'B').
     * @param isPrimary Not required nor expected (reserved for later use).
     *                  Designates whether the image is "primary". A crude way to get at
     *                  the semantics, which are still to be defined.
     * @param in        Required. The input stream with the image data. TIF, PNG, JPG and GIF images
     *                  are acceptable.
     * @param metadata  Required. Input metadata.
     * @return Http status of 200.
     *         If there is an error in the request, returns status 400.
     * @see MediaReferendType
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response imageUpload(
            @FormDataParam("objectType") String objectType,
            @FormDataParam("objectId") String objectId,
            @FormDataParam("primary") Boolean isPrimary, // indicates whether this image should be considered to be primary (not used)
            @FormDataParam("file") InputStream in,
            @FormDataParam("file") FormDataContentDisposition metadata,
            @Context HttpServletRequest request) {
        final long s = System.currentTimeMillis();
        final Response response = doUpload(objectType, objectId, in, metadata, request);
        try {
            getSystemManager().setResponseTime(UPLOAD_IMAGE_OPERATION, (System.currentTimeMillis() - s));
        } catch (SystemErrorException e) {
            logger.log(Level.SEVERE, "Failed to acquire SystemManager when attempting to upload image", e);
            return Response.status(500).entity("error=no sys mgr").build();
        }
        return response;
    }

    private Response doUpload(String objectType, String objectId, InputStream in, FormDataContentDisposition metadata, HttpServletRequest request) {
        final MediaReferendType referendType;
        try {
            BlahguaSession.ensureAuthenticated(request);
            try {
                referendType = MediaReferendType.valueOf(objectType);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "invalid object type '" + objectType + "'", e);
                return Response.status(400).entity("error=invalid object type").build();
            }
            if ((referendType != MediaReferendType.X) && (objectId == null)) {
                return Response.status(400).entity("error=missing object id").build();
            }
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "error=missing or invalid referendType. INFO:\n" + RestUtilities.getRequestInfo(request), e);
            return Response.status(500).entity("error=missing or invalid referendType").build();
        } catch (InvalidAuthorizedStateException e) {
            logger.log(Level.SEVERE, "Unauthorized access attempt", e);
            return Response.status(401).entity("error=unauthorized").build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to upload file to obj type '" + objectType + "' obj id '" + objectId + "'\nINFO:\n" + RestUtilities.getRequestInfo(request), e);
            return Response.status(500).entity("error=" + e.getMessage()).build();
        }

        AmazonS3 s3 = null;
        try {
            s3 = AWSUtilities.getAmazonS3();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to read AWSCredentials.properties file for resource stream. Failed to upload file to obj type '" + objectType + "' obj id '" + objectId + "'\nINFO:\n" + RestUtilities.getRequestInfo(request), e);
            return Response.status(400).entity("error=credentials error" + ((e.getMessage() == null) ? e.getClass() : e.getMessage())).build();
        }

        final String msg;
        try {
            msg = processFile(in, metadata, s3, referendType, objectId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to process file. INFO:\n" + RestUtilities.getRequestInfo(request), e);
            return Response.status(400).entity("error=Failed to process file: " + e.getMessage()).build();
        }
        return (msg == null) ? Response.status(400).build() :
                Response.status(200)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(msg).build();
    }

    private String processFile(InputStream in, FormDataContentDisposition metadata, AmazonS3 s3, MediaReferendType referendType, String objectId) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException {
        final long fileSize = metadata.getSize();
        if (fileSize != 0 && fileSize > 2000000) { // can be misleading or -1, but try...
            throw new InvalidRequestException("File size exceeds 2MB limit: " + fileSize);
        }
        final String file = metadata.getFileName();
        final String extension = getExtension(file);
        if (!isFormatSupported(extension)) {
            throw new InvalidRequestException("File format '" + extension + "' is not supported: " + file, ErrorCodes.UNSUPPORTED_MEDIA_TYPE);
        }
        final String filename = getFilename(file);

        final MediaDAO mediaDAO = makeMediaRecord(referendType);
        final String filepath = localOriginalImagePath + mediaDAO.getId() + extension;
        final File infile = new File(filepath);
        saveFile(in, infile, s3);
        saveFormats(infile, s3, mediaDAO.getId(), referendType, objectId);

        return mediaDAO.getId();
    }

    private MediaDAO makeMediaRecord(MediaReferendType referendType) throws SystemErrorException {
        final MediaDAO media = getStoreManager().createMedia();
        media.setReferendType(referendType.toString());
        media.setType(com.eweware.service.base.store.dao.type.MediaType.I.toString());
        media._insert();
        return media;
    }

    private boolean isFormatSupported(String extension) {
        return (extension != null) && (supportedUploadFormats.get(extension) != null);
    }

    public static void main(String[] a) {
        System.out.println(1000 / 10000000);
    }
    private void saveFormats(File original, AmazonS3 s3, String mediaId, MediaReferendType referendType, String objectId) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException {

        final String filename = original.getName();
        final String filepath = original.getAbsolutePath();
        final ConvertCmd cmd = new ConvertCmd();

        final List<File> filesToDelete = new ArrayList<File>();
        try {
            final Info imageInfo = new Info(filepath, true);
            final int imageWidth = imageInfo.getImageWidth();
            final int imageHeight = imageInfo.getImageHeight();

            for (FileTypeSpec spec : FileTypeSpec.values()) {

                final String newFilename = makeFilename(true, spec, filename);

                IMOperation op = new IMOperation();
                op.addImage();
                op.autoOrient(); // BWC-1540

                if (spec.mode == TypeSpecMode.FIXED) {
                    if (imageWidth == imageHeight) {    // square image
                        if (imageWidth != spec.width) {
                            op.resample(DEFAULT_IMAGE_DENSITY);
                            op.scale(spec.width, spec.height);
                        } else {
                            // do nothing: it's what we want
                        }
                    } else { // non-square image
                        op.resample(DEFAULT_IMAGE_DENSITY);
                        if (imageWidth > imageHeight) {
                            op.scale(null, spec.height);
                        } else if (imageWidth < imageHeight) {
                            op.scale(spec.width, null);
                        }
                        op.crop(spec.width, spec.height, 0, 0);
                    }
                } else if (spec.mode == TypeSpecMode.WIDTH_DOMINANT) {
                    int width = imageWidth;
                    if (width > spec.width) {
                        width = spec.width;
                    }
                    int scale = imageWidth / width;
                    int height = (imageHeight / (scale == 0 ? 1 : scale));
                    op.resample(DEFAULT_IMAGE_DENSITY);
                    op.resize(width, height);
//                    op.density(DEFAULT_IMAGE_DENSITY, DEFAULT_IMAGE_DENSITY);
                    logger.info("Saving " + width + "x" + height);
                } else if (spec.mode == TypeSpecMode.HEIGHT_DOMINANT) {
                    op.resample(DEFAULT_IMAGE_DENSITY);
                    op.scale(null, spec.height); // TODO not used, left to complete
                }

                final String newImagePathname = localFormattedImagePath + "/" + newFilename;
                op.addImage();

                cmd.run(op, filepath, newImagePathname);

                final File newFile = new File(newImagePathname);
                if (!newFile.exists()) {
                    throw new SystemErrorException("File " + newFilename + " was not successfully converted", ErrorCodes.SERVER_SEVERE_ERROR);
                }
                filesToDelete.add(newFile);
                // Upload converted file to s3
                try {
                    final long start = System.currentTimeMillis();
                    s3.putObject(new PutObjectRequest(getMediaManager().getImageBucketName(), getMediaManager().getBucketImageDir() + newFilename, newFile));
//                    System.out.println(newFilename + " SAVED TO S3 IN " + (System.currentTimeMillis() - start) + "ms");
                } catch (com.amazonaws.AmazonServiceException e) {
                    throw new SystemErrorException("AWS service exception when putting " + filepath + " into s3", e, ErrorCodes.SEVERE_AWS_ERROR);
                } catch (com.amazonaws.AmazonClientException e) {
                    throw new SystemErrorException("AWS client exception when putting " + filepath + " into s3", e, ErrorCodes.SEVERE_AWS_ERROR);
                } catch (Exception e) {
                    throw new SystemErrorException("Exception when putting " + filepath + " into s3", e, ErrorCodes.SEVERE_AWS_ERROR);
                }
            }
            associateImageWithObject(mediaId, referendType, objectId);


        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception ex) {
            throw new SystemErrorException("File upload failed", ex, ErrorCodes.SERVER_RECOVERABLE_ERROR);
        } finally {
            for (File file : filesToDelete) {
                try {
                    file.delete();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to delete processed file: " + file.getAbsolutePath(), e);
                }
            }
            try {
                original.delete();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to delete original file: " + filepath, e);
            }
        }
    }

    private void associateImageWithObject(String mediaId, MediaReferendType referendType, String objectId) throws SystemErrorException, ResourceNotFoundException {
        List<String> imageIds = new ArrayList<String>(1);
        imageIds.add(mediaId);
//        logger.finer("*** uploading image media id '" + mediaId + "' object type '" + referendType + "' object id '" + objectId + "' ***");
        if (referendType == MediaReferendType.B) {
            final BlahDAO blah = getStoreManager().createBlah(objectId);
            if (!blah._exists()) {
                throw new ResourceNotFoundException("No blahId '" + objectId + "' exists", ErrorCodes.NOT_FOUND_BLAH_ID);
            }
            blah.setImageIds(imageIds);
            blah._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
        } else if (referendType == MediaReferendType.C) {
            getBlahManager().associateImageWithComment(objectId, mediaId);
        } else if (referendType == MediaReferendType.U) {
            getUserManager().associateImageWithUser(objectId, mediaId, false);
        } else if (referendType == MediaReferendType.X) {
            // do nothing: the image may be associated with an object later on
        } else {
            throw new SystemErrorException("Media referend type '" + referendType + "' not supported", ErrorCodes.SERVER_SEVERE_ERROR);
        }
    }

    private String makeFilename(boolean upload, FileTypeSpec spec, String filename) throws InvalidRequestException {
        final String extension = getExtension(filename);
        if (extension == null) {
            throw new InvalidRequestException("Extension missing from image filename: " + filename, ErrorCodes.UNSUPPORTED_MEDIA_TYPE);
        }
        final String imageName = getFilename(filename);
        final StringBuilder b = new StringBuilder();
        b.append(imageName);
        b.append("-");
        b.append(spec);
        b.append(canonicalImageFileFormat);
        return b.toString();
    }

    private void saveFile(InputStream in, File infile, AmazonS3 s3) throws SystemErrorException {

        saveLocalFile(in, infile);
        try {
            s3.putObject(new PutObjectRequest(getMediaManager().getImageBucketName(), getMediaManager().getBucketOriginalDir() + infile.getName(), infile));
        } catch (com.amazonaws.AmazonServiceException e) {
            throw new SystemErrorException("AWS service exception when putting " + infile.getAbsolutePath() + " into s3", e, ErrorCodes.SEVERE_AWS_ERROR);
        } catch (com.amazonaws.AmazonClientException e) {
            throw new SystemErrorException("AWS client exception when putting " + infile.getAbsolutePath() + " into s3", e, ErrorCodes.SEVERE_AWS_ERROR);
        } catch (Exception e) {
            throw new SystemErrorException("Exception when putting " + infile.getAbsolutePath() + " into s3", e, ErrorCodes.SEVERE_AWS_ERROR);
        }
    }

    /**
     * Returns extension with dot (e.g., ".jpg")
     */
    private String getExtension(String fileName) {
        final int dot = fileName.lastIndexOf(".");
        return (dot == -1) ? null : fileName.substring(dot);
    }

    /**
     * Returns the file name (omits any extension)
     */
    private String getFilename(String fileNameWithPossibleExtension) {
        final int dot = fileNameWithPossibleExtension.lastIndexOf(".");
        if (dot == -1) {
            return fileNameWithPossibleExtension;
        } else {
            return fileNameWithPossibleExtension.substring(0, dot);
        }
    }

    /**
     * Saves the uploaded file in the local fs for further processing
     */
    private void saveLocalFile(InputStream in, File infile) throws SystemErrorException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(infile);

            int read = 0;
            byte[] bytes = new byte[1024];
//            int bytecount = 0;
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
//                bytecount += read;
            }
            out.flush();
//            System.out.println("Saved " + infile.getAbsolutePath() + " (" + bytecount + " bytes)");
        } catch (IOException e) {
            throw new SystemErrorException("Failed to save uploaded file " + infile.getAbsolutePath(), e, ErrorCodes.SERVER_SEVERE_ERROR);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new SystemErrorException("Failed to close io for local file " + infile.getAbsolutePath(), e, ErrorCodes.SERVER_SEVERE_ERROR);
                }
            }
        }
    }

    private UserManager getUserManager() throws SystemErrorException {
        if (userManager == null) {
            userManager = UserManager.getInstance();
        }
        return userManager;
    }

    private BlahManager getBlahManager() throws SystemErrorException {
        if (blahManager == null) {
            blahManager = BlahManager.getInstance();
        }
        return blahManager;
    }


    private MediaManager getMediaManager() throws SystemErrorException {
        if (mediaManager == null) {
            mediaManager = MediaManager.getInstance();
        }
        return mediaManager;
    }

    private StoreManager getStoreManager() throws SystemErrorException {
        if (storeManager == null) {
            storeManager = MongoStoreManager.getInstance();
        }
        return storeManager;
    }

    private SystemManager getSystemManager() throws SystemErrorException {
        if (systemManager == null) {
            systemManager = SystemManager.getInstance();
        }
        return systemManager;
    }
}


//    @POST
//    @Path("/upload")
//    public Response upload(@Context HttpServletRequest request) {
//        String error = null;
//        String msg = "";
//        if (ServletFileUpload.isMultipartContent(request)) {
//            msg = "got file in request";
//            // Create a factory for disk-based file items
//            DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
//            String path = request.getRealPath("") + File.separatorChar + "publishFiles" + File.separatorChar;
//            // File f = new File(path + "myfile.txt");
//            // File localOriginalImagePath = new File("c:\\tmp");
//            File destinationDir = new File(path);
//            // Set the size threshold, above which content will be stored on disk.
//            // fileItemFactory.setSizeThreshold(1*1024*1024); //1 MB
//
//            // Set the temporary directory to store the uploaded files of size above threshold.
//            // fileItemFactory.setRepository(localOriginalImagePath);
//
//            // Create a new file upload handler
//            ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
//            uploadHandler.setFileSizeMax(1000000L);
//            try {
//                List items = uploadHandler.parseRequest(request);
//                Iterator itr = items.iterator();
//
//                while (itr.hasNext()) {
//                    FileItem item = (FileItem) itr.next();
//                    /*
//                     * Handle Form Fields.
//                     */
//                    if (item.isFormField()) {
//                        msg += "<BR>" + "Field Name = " + item.getFieldName() + ", Value = " + item.getString();
//                    } else {
//                        //Handle Uploaded files.
//                        msg += "<BR>" + "File Field Name = " + item.getFieldName() +
//                                ", File Name = " + item.getName() +
//                                ", Content type = " + item.getContentType() +
//                                ", File Size = " + item.getSize();
//                        /*
//                         * Write file to the ultimate location.
//                         */
//                        File file = new File(destinationDir, item.getName());
//                        item.write(file);
//                    }
//                }
//            } catch (FileUploadException ex) {
//                error = "Error encountered while parsing the request " + ex;
//            } catch (Exception ex) {
//                error = "Error encountered while uploading file " + ex;
//            }
//        }
//
//        return (error != null) ? Response.status(400).entity(error).build() : Response.status(200).entity(msg).build();
//    }

