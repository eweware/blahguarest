package com.eweware.service.base.store.dao;

import com.mongodb.DBObject;
import com.eweware.service.base.error.DuplicateKeyException;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.type.DAOUpdateType;

import java.util.List;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/2/12 Time: 4:56 PM
 */
public interface DAOMethods {

    /**
     * Initializes the dao to its default values, if any.
     * The defaults are based on the dao's schema.
     */
    public void initToDefaultValues(LocaleId localeId) throws SystemErrorException;

    /**
     * Add the field/value pairs in the map to this dao.
     * The map's fields will be validated. Fields that are
     * not in the schema will be ignored and invalid values
     * will throw an error.
     *
     * @param map                A map with field/value pairs.
     * @param validateAndConvert If true, the fields in the specified map will
     *                           be validated against the schema and converted
     *                           to the internal DB data types if necessary.
     */
    public abstract void addFromMap(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException;

    /**
     * Creates the DAO if it doesn't exist, otherwise it assumes
     * that this is an incremental update.
     * TODO requires use of DBObject. The extra work and indirection is not justified: treat it as an exception.
     * @query The query object.
     * @throws SystemErrorException
     */
    public abstract void _upsert(DBObject query) throws SystemErrorException;

    /**
     * Updates this dao using the non-null fields in this object.
     * This dao must have a primary id value.
     * Updates in this method are additive--no fields are deleted
     * as a result of executing this method.
     * If the underlying implementation supports it, the
     * update will be atomic.
     * <p/>
     *
     * @param updateType The type of update (see enum for documentation)
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     */
    public abstract void _updateByPrimaryId(DAOUpdateType updateType) throws SystemErrorException, DuplicateKeyException;

    /**
     * Updates this dao using existing fields in this object.
     * This update is used for compound key records.
     *
     * @param updateType The type of update (see enum for documentation)
     * @param ids        The compound key ids.
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     */
    public abstract void _updateByCompoundId(DAOUpdateType updateType, String... ids) throws SystemErrorException, DuplicateKeyException;

    /**
     * Inserts this dao using the non-null fields in this object.
     * If the insert is successful, the primary id field will set
     * to the dao's new id.
     * <p/>
     * TODO throw AlreadyExistsInDBException
     *
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     */
    public abstract void _insert() throws SystemErrorException, DuplicateKeyException;

    /**
     * Deletes dao in database. This dao must have a primary id value.
     *
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     */
    public abstract void _deleteByPrimaryId() throws SystemErrorException;

    /**
     * Deletes all db objects that match the specified composite id.
     *
     * @param idFieldNames Names of fields that make up the composite id. Left-to-right
     *                     order of the listed id names might be important in the underlying
     *                     implementation if the fields are indexed.
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     */
    public abstract void _deleteByCompositeId(String... idFieldNames) throws SystemErrorException;

    /**
     * The existence check will try to find a match using all non-null fields in the dao.
     * This method does not side-effect this instance's data.
     *
     * @return boolean  True if the object exists in the database
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     */
    public abstract boolean _exists() throws SystemErrorException;

    /**
     * @return int  Returns number of records that match this object.
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     */
    public abstract long _count() throws SystemErrorException;

    /**
     * Finds objects in a field's range and optionally sorts them.
     *
     * @param sort          If true, the objects will be sorted by the fieldName
     * @param fieldName     The name of the field for the range find
     * @param from          Start of range. This is required. (Could be implemented, but
     *                      it might result in some unfortunate problems if there's a client
     *                      bug in a production environment.
     * @param fromInclusive True if from should be included in the results
     * @param to            End of range. This is optional: if not provided, all objects after this value will be returned.
     * @param toInclusive   True if the to should be included in the results
     * @return List<? extends BaseDAO> List of objects in range or an empty list, possibly sorted.
     */
    public abstract List<? extends BaseDAO> _findRangeSingleField(boolean sort, String fieldName, String from, boolean fromInclusive, String to, boolean toInclusive) throws SystemErrorException;

    /**
     * Returns all object of the type of this dao. Set fields
     * are used to match the elements returned.
     * @return  List<BaseDAO>   Returns possibly empty list of daos.
     * @throws SystemErrorException
     */
    public List<? extends BaseDAO> _findMany() throws SystemErrorException;

    /**
     * Returns all objects of the type of this dao.
     *
     * @param start         Optional start index (null if none)
     * @param count         Optional max number of items to get (ignored if start is null)
     * @param sortFieldName Optional    If non-null, this is the name of a field to sort by
     * @return List<BaseDAO>    Returns possibly empty list of daos.
     * @throws com.eweware.service.base.error.SystemErrorException
     *          TODO should also support fields to return hint
     */
    public abstract List<? extends BaseDAO> _findMany(Integer start, Integer count, String sortFieldName) throws SystemErrorException;

//    /**
//     * For each primary field id value, tries to find a db object with
//     * that key and returns it in the resulting list.
//     *
//     * @param primaryIds List of primary key id values
//     * @return List<BaseDAO>    Returns possibly empty list of daos.
//     * @throws com.eweware.service.base.error.SystemErrorException
//     *          TODO should also support fields to return hint
//     */
//    public abstract List<? extends BaseDAO> _findManyByPrimaryIds(String[] primaryIds) throws SystemErrorException;

    /**
     * Returns a list of objects that match the criteria or an empty list if no such
     * object is found.
     *
     * @param start              Optional start index (null if none)
     * @param count              Optional max number of items to get (ignored if start is null)
     * @param sortFieldName      If non-null, this is the name of a field to sort by
     * @param fieldsToReturnHint A list of field names as a hint of which fields must be returned
     *                           (i.e., all other fields need not be returned). This value may be null.
     * @param idFieldNames       Names of fields that make up the composite id. Left-to-right
     *                           order of the listed id names might be important in the underlying
     *                           implementation if the fields are indexed.
     * @return List<BaseDAO>    Returns possibly empty list of daos.
     * @throws com.eweware.service.base.error.SystemErrorException
     *          TODO should also support fields to return hint
     */
    public abstract List<? extends BaseDAO> _findManyByCompositeId(Integer start, Integer count, String sortFieldName, String[] fieldsToReturnHint, String... idFieldNames) throws SystemErrorException;

    /**
     * Returns the first object that matches the criteria or null if no such
     * object is found.
     *
     * @param fieldsToReturnHint A list of field names as a hint of which fields must be returned
     *                           (i.e., all other fields need not be returned). This value may be null.
     * @param idFieldNames       Names of fields that make up the composite id. Left-to-right
     *                           order of the listed id names might be important in the underlying
     *                           implementation if the fields are indexed.
     * @return BaseDAO  Returns the first object found, or null if there is none.
     * @throws com.eweware.service.base.error.SystemErrorException
     *          TODO should also support fields to return hint
     */
    public abstract BaseDAO _findByCompositeId(String[] fieldsToReturnHint, String... idFieldNames) throws SystemErrorException;

    /**
     * Returns the object that matches the primary id or null if it doesn't exist.
     *
     * @param fieldsToReturnHint Indicates interest in only the specified fields. If possible
     *                           the implementation will exclude all other fields from the returned dao.
     * @return BaseDAO  Returns unique object or null if it can't be found. Note that the
     *         client should only expect the fields enumerated in fieldsToReturnHint.
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     */
    public abstract BaseDAO _findByPrimaryId(String... fieldsToReturnHint) throws SystemErrorException;

}
