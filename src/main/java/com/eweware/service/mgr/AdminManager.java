package main.java.com.eweware.service.mgr;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import main.java.com.eweware.service.base.AWSConstants;
import main.java.com.eweware.service.base.AWSUtilities;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.store.dao.*;
import main.java.com.eweware.service.base.store.dao.type.DAOUpdateType;
import main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import main.java.com.eweware.service.rest.resource.ImageUploadResource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rk@post.harvard.edu
 *         Date: 4/8/13 Time: 12:44 PM
 */
public class AdminManager {

    private static final Logger logger = Logger.getLogger(AdminManager.class.getName());

    private static AdminManager singleton;
    private  MongoStoreManager storeManager;

    public static AdminManager getInstance() throws SystemErrorException {
        if (singleton == null) {
            singleton = new AdminManager();
            singleton.storeManager = MongoStoreManager.getInstance();
        }
        return singleton;
    }

    public void deleteBlah(String blahId) throws SystemErrorException {
        try {
            deleteBlahComments(blahId);
        } catch (SystemErrorException e) {
            throw new SystemErrorException("Failed to delete some comments for blah id '" + blahId + "'", e, ErrorCodes.SERVER_SEVERE_ERROR);
        }

        final BlahDAO dao = (BlahDAO) storeManager.createBlah(blahId)._findByPrimaryId(BlahDAO.IMAGE_IDS, BlahDAO.GROUP_ID);
        if (dao != null) {
            try {
                deleteImages(dao.getImageIds());
            } catch (SystemErrorException e) {
                throw new SystemErrorException("Failed to delete some images for blah id '" + blahId + "'", e, ErrorCodes.SERVER_SEVERE_ERROR);
            }
            final String groupId = dao.getGroupId();
            try {
                dao._deleteByPrimaryId();
            } catch (SystemErrorException e) {
                throw new SystemErrorException("Failed to delete blah dao id '" + blahId + "'", e, ErrorCodes.SERVER_SEVERE_ERROR);
            }
            if (groupId != null) {
                final GroupDAO groupDAO = storeManager.createGroup(groupId);
                groupDAO.setBlahCount(-1);
                groupDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
            }
        }

        final BlahTrackerDAO btQuery = storeManager.createBlahTracker();
        btQuery.put(BlahTrackerDAO.BT_OBJECT_ID, blahId);
        try {
            if (btQuery._exists()) {
                btQuery._deleteByCompositeId(BlahTrackerDAO.BT_OBJECT_ID);
            }
        } catch (Exception e) {
            throw new SystemErrorException("Exception ignored: Failed to delete tracker for blah id '" + blahId + "'", e, ErrorCodes.SERVER_SEVERE_ERROR);
        }
    }

    public void removeUserBlahs(String userId) throws SystemErrorException {
        final BlahDAO blahDAO = storeManager.createBlah();
        blahDAO.setAuthorId(userId);
        final List<BlahDAO> blahDAOs = (List<BlahDAO>) blahDAO._findManyByCompositeId(null, null, null, new String[]{BlahDAO.AUTHOR_ID}, BlahDAO.AUTHOR_ID);
        for (BlahDAO dao : blahDAOs) {
            deleteBlah(dao.getId());
        }
    }

    public void deleteUser(String userId) throws SystemErrorException {

        removeUserBlahs(userId);

        // Delete user/group association
        final UserGroupDAO userGroup = storeManager.createUserGroup();
        userGroup.setUserId(userId);
        final List<UserGroupDAO> userGroupDAOs = (List<UserGroupDAO>) userGroup._findManyByCompositeId(null, null, null, new String[]{UserGroupDAOConstants.GROUP_ID}, UserGroupDAOConstants.USER_ID);
        if (userGroupDAOs.size() > 0) {
            final java.util.Set<String> groupIds = new HashSet<String>(userGroupDAOs.size());
            for (UserGroupDAO ugdao : userGroupDAOs) {
                ugdao._deleteByPrimaryId();
                groupIds.add(ugdao.getGroupId());
            }
            // Reduce user counts for groups
            for (String groupId : groupIds) {
                final GroupDAO groupDAO = storeManager.createGroup(groupId);
                groupDAO.setUserCount(-1);
                groupDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
            }
        }

        // Delete user
        storeManager.createUser(userId)._deleteByPrimaryId();

        // Delete user account
        storeManager.createUserAccount(userId)._deleteByPrimaryId();
    }

    public void clearGroupBlahs(String groupId) throws SystemErrorException {
        final BlahDAO blah = storeManager.createBlah();
        blah.setGroupId(groupId);
        final List<BlahDAO> blahDAOs = (List<BlahDAO>) blah._findManyByCompositeId(null, null, null, new String[]{BlahDAO.ID}, BlahDAO.GROUP_ID);
        for (BlahDAO blahDAO : blahDAOs) {
            final String blahId = blahDAO.getId();
            deleteBlah(blahId);
        }
    }

    private void deleteBlahComments(String blahId) throws SystemErrorException {
        final CommentDAO query = storeManager.createComment();
        query.setBlahId(blahId);
        final List<CommentDAO> comments = (List<CommentDAO>) query._findManyByCompositeId(null, null, null, new String[]{CommentDAO.IMAGE_IDS}, CommentDAO.BLAH_ID);
        for (CommentDAO commentDAO : comments) {

            final String commentId = commentDAO.getId();
            final List<String> imageIds = commentDAO.getImageIds();

            try {
                deleteImages(imageIds);
            } catch (Exception e) {
                throw new SystemErrorException("Failed to delete images for comment id '" + commentId + "'", e, ErrorCodes.SERVER_SEVERE_ERROR);
            }

            try {
                deleteMediaDAOs(imageIds);
            } catch (Exception e) {
                throw new SystemErrorException("Failed to delete media records for comment id '" + commentId + "'", e, ErrorCodes.SERVER_SEVERE_ERROR);
            }
            try {
                commentDAO._deleteByPrimaryId();
            } catch (Exception e) {
                throw new SystemErrorException("Failed to delete comment id '" + commentId + "'", e, ErrorCodes.SERVER_SEVERE_ERROR);
            }

            final CommentTrackerDAO ctQuery = storeManager.createCommentTracker();
            ctQuery.put(CommentTrackerDAO.CT_OBJECT_ID, commentId);
            try {
                if (ctQuery._exists()) {
                    ctQuery._deleteByCompositeId(CommentTrackerDAO.CT_OBJECT_ID);
                }
            } catch (Exception e) {
                throw new SystemErrorException("Exception ignored: Failed to delete tracker for comment id '" + commentId + "'", e, ErrorCodes.SERVER_SEVERE_ERROR);
            }
        }
    }

    private void deleteImages(List<String> imageIds) throws SystemErrorException {
        if (imageIds != null && imageIds.size() > 0) {
            final AmazonS3 s3 = AWSUtilities.getAmazonS3();
            final String bucketName = MediaManager.getInstance().getImageBucketName();
            final String imagePath = MediaManager.getInstance().getBucketImageDir();
            for (String imageId : imageIds) {
                for (String suffix : AWSConstants.AWS_S3_IMAGE_SUFFIXES) {
                    final StringBuilder key = new StringBuilder(imagePath);
                    key.append(imageId);
                    key.append(suffix);
                    key.append(ImageUploadResource.canonicalImageFileFormat);
                    try {
                        s3.deleteObject(new DeleteObjectRequest(bucketName, key.toString()));
                    } catch (Exception e) {
                        throw new SystemErrorException("Failed to delete processed image id '" + key.toString() + "' out of " + imageIds, e, ErrorCodes.SERVER_SEVERE_ERROR);
                    }
                }
                final String originalsPath = MediaManager.getInstance().getBucketOriginalDir();
                try {
                    final StringBuilder key = new StringBuilder(originalsPath);
                    key.append(imageId);
                    key.append(ImageUploadResource.canonicalImageFileFormat);
                    s3.deleteObject(new DeleteObjectRequest(bucketName, key.toString()));
                } catch (Exception e) {
                    throw new SystemErrorException("Failed to delete original image id '" + imageId + "' out of " + imageIds, e, ErrorCodes.SERVER_SEVERE_ERROR);
                }
            }
            deleteMediaDAOs(imageIds);
        }
    }

    private void deleteMediaDAOs(List<String> mediaIds) throws SystemErrorException {
         List<String> failedIds = null;
        for (String id : mediaIds) {
            final MediaDAO dao = storeManager.createMedia();
            try {
                dao.setId(id);
                dao._deleteByPrimaryId();
            } catch (Exception e) {
                if (failedIds == null) {
                    failedIds = new ArrayList<String>();
                }
                failedIds.add(id);
                logger.log(Level.SEVERE, "Failed to delete media id '" + id + "'", e);
            }
        }
        if (failedIds != null) {
            throw new SystemErrorException("Failed to delete following media ids (errors logged): " + failedIds);
        }
    }


//    public static AmazonS3 getAmazonS3() throws SystemErrorException {
//        try {
//            final String filename = "/Users/admin/dev/blahgua/beta/blahguarest/src/main/resources/AwsCredentials.properties";
//            final File file = new File(filename);
//            if (file.exists()) {
//                return new AmazonS3Client(new PropertiesCredentials(file));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public static void main(String[] a) {
//        try {
//            final AmazonS3 s3 = getAmazonS3();
//            if (s3 != null) {
//               s3.deleteObject(new DeleteObjectRequest("blahguaimages", "image/horse.jpg"));
////                final ObjectMetadata meta = obj.getObjectMetadata();
////                final String contentType = meta.getContentType();
////                final Date lastModified = meta.getLastModified();
////                System.out.println(contentType + " " + lastModified);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
