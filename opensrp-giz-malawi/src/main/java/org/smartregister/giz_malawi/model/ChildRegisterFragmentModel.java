package org.smartregister.giz_malawi.model;

import org.smartregister.AllConstants;
import org.smartregister.child.model.BaseChildRegisterFragmentModel;
import org.smartregister.giz_malawi.util.DBConstants;

/**
 * Created by ndegwamartin on 2019-05-27.
 */
public class ChildRegisterFragmentModel extends BaseChildRegisterFragmentModel {
    @Override
    protected String[] mainColumns(String tableName, String parentTableName) {
        String[] columns = new String[]{

                tableName + "." + DBConstants.KEY.RELATIONALID,
                tableName + "." + DBConstants.KEY.DETAILS,
                tableName + "." + DBConstants.KEY.MER_ID,
                tableName + "." + DBConstants.KEY.RELATIONAL_ID,
                tableName + "." + DBConstants.KEY.FIRST_NAME,
                tableName + "." + DBConstants.KEY.LAST_NAME,
                tableName + "." + AllConstants.ChildRegistrationFields.GENDER,
                tableName + "." + DBConstants.KEY.BASE_ENTITY_ID,
                parentTableName + "." + DBConstants.KEY.FIRST_NAME + " as mother_first_name",
                parentTableName + "." + DBConstants.KEY.LAST_NAME + " as mother_last_name",
                parentTableName + "." + DBConstants.KEY.DOB + " as mother_dob",
                parentTableName + "." + DBConstants.KEY.NRC_NUMBER + " as mother_nrc_number",
                tableName + "." + DBConstants.KEY.FATHER_FIRST_NAME,
                tableName + "." + DBConstants.KEY.DOB,
                tableName + "." + DBConstants.KEY.CONTACT_PHONE_NUMBER,
                tableName + "." + DBConstants.KEY.PROVIDER_UC,
                tableName + "." + DBConstants.KEY.PROVIDER_TOWN,
                tableName + "." + DBConstants.KEY.PROVIDER_ID,
                tableName + "." + DBConstants.KEY.PROVIDER_LOCATION_ID,
                tableName + "." + DBConstants.KEY.CLIENT_REG_DATE,
                tableName + "." + DBConstants.KEY.LAST_INTERACTED_WITH
        };
        return columns;
    }
}
