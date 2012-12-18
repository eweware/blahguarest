package main.java.com.eweware.service.rest.resource;


import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.InvalidRequestException;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.Info;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.HashMap;

/**
 * @author rk@post.harvard.edu
 *         Date: 12/14/12 Time: 3:44 PM
 *         <p/>
 *         Need to create jersey-multipart-config.properties and add the following
 *         <p/>
 *         bufferThreshold = 128000
 */
@Path("/images")
public class ImageUploadResource {

    /**
     * These paths should be part of the config
     */
    private static final String tmpDir = "/app/blahguarest/images/tmp/";
    private static final String s3Dir = "/app/blahguarest/images/s3/";
    private static final String canonicalFileFormat = ".jpg";
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
    }


    private enum FileTypeSpec {
        A(128, 128, TypeSpecMode.FIXED),
        B(256, 256, TypeSpecMode.FIXED),
        C(512, 512, TypeSpecMode.FIXED),
        D(768, null, TypeSpecMode.WIDTH_DOMINANT);

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
        FIXED,
        WIDTH_DOMINANT,
        HEIGHT_DOMINANT
    }

    @GET
    @Path("/download/{blahId}/{fileType}/{filename}")
    @Produces(MediaType.MULTIPART_FORM_DATA)
    public Response hello(@PathParam("blahId") String blahId,
                          @PathParam("fileType") String fileType,
                          @PathParam("filename") String filename) {
        // ignore blahId
        final FileTypeSpec fileTypeSpec;
        try {
            fileTypeSpec = FileTypeSpec.valueOf(fileType);
        } catch (Exception e) {
            return Response.status(400).entity("Invalid image type: " + e.getMessage()).build();
        }
        final File dir = new File(s3Dir);
        if (!dir.exists()) {
            return Response.status(400).entity("Directory " + dir.getAbsolutePath() + " doesn't exist").build();
        }
        try {
            filename = makeFilename(false, fileTypeSpec, filename);
        } catch (InvalidRequestException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
        final File file = new File(s3Dir, filename);
        if (!file.exists()) {
            return Response.status(400).entity("File " + filename + " doesn't exist").build();
        }
        try {
            System.out.println("Downloading " + file.getAbsolutePath());
            final Response response = Response.status(200).entity(file).type(MediaType.MULTIPART_FORM_DATA_TYPE).build();
            System.out.println("... downloaded");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response imageUpload(
            @FormDataParam("file") InputStream in,
            @FormDataParam("file") FormDataContentDisposition metadata,
            @Context HttpServletRequest request) {

        final String tmpDirPathname = tmpDir; // request.getRealPath("") + File.separatorChar + "WEB-INF/images_tmp" + File.separatorChar;
        final String s3Pathname = s3Dir; //request.getRealPath("") + File.separatorChar + "images" + File.separatorChar;

        final String msg;
        try {
            msg = processFile(in, metadata, tmpDirPathname, s3Pathname);
        } catch (InvalidRequestException x) {
            return Response.status(400).entity(x.getMessage()).build();
        }
        return (msg == null) ? Response.status(400).build() : Response.status(200).entity(msg).build();
    }

    private String processFile(InputStream in, FormDataContentDisposition metadata, String tmpDirPathname, String s3Pathname) throws InvalidRequestException {
        final String fileName = metadata.getFileName();
        if (!isSupported(fileName)) {
            throw new InvalidRequestException("File format not supported: " + fileName, ErrorCodes.UNSUPPORTED_MEDIA_TYPE);
        }
        final File original = new File(tmpDirPathname + fileName);

        saveFile(in, metadata, tmpDirPathname, original);

        saveFormat(original, s3Pathname);

        return "OK";
    }

    private boolean isSupported(String fileName) {
        final int dot = fileName.indexOf(".");
        return (dot != -1) && (supportedUploadFormats.get(fileName.substring(dot)) != null);
    }

    private void saveFormat(File original, String s3Pathname) throws InvalidRequestException {

        final String filename = original.getName();
        final String filepath = original.getAbsolutePath();
        final ConvertCmd cmd = new ConvertCmd();
        try {
            final Info imageInfo = new Info(filepath, true   );
//            System.out.println("Format: " + imageInfo.getImageFormat());
//            System.out.println("Width: " + imageInfo.getImageWidth());
//            System.out.println("Height: " + imageInfo.getImageHeight());
//            System.out.println("Geometry: " + imageInfo.getImageGeometry());
//            System.out.println("Depth: " + imageInfo.getImageDepth());
//            System.out.println("Class: " + imageInfo.getImageClass());

            final int imageWidth = imageInfo.getImageWidth();
            final int imageHeight = imageInfo.getImageHeight();

            for (FileTypeSpec spec : FileTypeSpec.values()) {

                final String newFilename = makeFilename(true, spec, filename);

                IMOperation op = new IMOperation();
                op.addImage();

                if (spec.mode == TypeSpecMode.FIXED) {
                    if (imageWidth == imageHeight) {    // square image
                        if (imageWidth != spec.width) {
                            op.scale(spec.width, spec.height);
                        } else {
                            // do nothing: it's what we want
                        }
                    } else { // non-square image
                        double factor = 0d;
                        if (imageWidth > imageHeight) {
                            op.scale(null, spec.height);
                        } else if (imageWidth < imageHeight) {
                            op.scale(spec.width, null);
                        }
                        op.crop(spec.width, spec.height);
                    }
                } else if (spec.mode == TypeSpecMode.WIDTH_DOMINANT) {
                    op.scale(spec.width, null);
                } else if (spec.mode == TypeSpecMode.HEIGHT_DOMINANT) {
                    op.scale(null, spec.height);
                }
                final String newImagePathname = s3Pathname + "/" + newFilename;
                op.addImage();
                cmd.run(op, filepath, newImagePathname);
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private String makeFilename(boolean upload, FileTypeSpec spec, String filename) throws InvalidRequestException {
        final int dot = filename.lastIndexOf(".");
        if (dot == -1) {
            throw new InvalidRequestException("Invalid image filename: missing extension", ErrorCodes.UNSUPPORTED_MEDIA_TYPE);
        }
        final StringBuilder b = new StringBuilder();
        final String name = filename.substring(0, dot);
        b.append(name);
        b.append("-");
        b.append(spec);
        if (!upload && spec != FileTypeSpec.D) {
            b.append("-0");          // XXX can we get around this exception? maybe there's an output filename param.
        }
        b.append(canonicalFileFormat);
        return b.toString();
    }

    private void saveFile(InputStream in, FormDataContentDisposition metadata, String tmpDirPathname, File original) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(original);
            int read = 0;
            byte[] bytes = new byte[1024];
            int bytecount = 0;
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
                bytecount += read;
            }
            out.flush();
            System.out.println("Saved " + metadata + " (" + bytecount + " bytes) to " + tmpDirPathname);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
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
//            // File tmpDir = new File("c:\\tmp");
//            File destinationDir = new File(path);
//            // Set the size threshold, above which content will be stored on disk.
//            // fileItemFactory.setSizeThreshold(1*1024*1024); //1 MB
//
//            // Set the temporary directory to store the uploaded files of size above threshold.
//            // fileItemFactory.setRepository(tmpDir);
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