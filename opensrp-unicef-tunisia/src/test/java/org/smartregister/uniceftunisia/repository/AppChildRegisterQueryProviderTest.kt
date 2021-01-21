package org.smartregister.uniceftunisia.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class AppChildRegisterQueryProviderTest {

    @Test
    fun `Should return main query`() {
        val  appChildRegisterQueryProvider = AppChildRegisterQueryProvider()
        assertEquals("select ec_client.id as _id,ec_client.relationalid ,ec_client.zeir_id ,ec_client.gender " +
                ",ec_client.base_entity_id ,ec_client.first_name ,ec_client.last_name ,ec_client.village " +
                ",ec_client.home_address ,ec_client.dob ,ec_client.client_reg_date ,ec_client.last_interacted_with " +
                ",ec_mother_details.mother_nationality ,ec_mother_details.mother_nationality_other " +
                ",ec_mother_details.protected_at_birth ,ec_mother_details.mother_tdv_doses " +
                ",ec_mother_details.first_birth ,ec_mother_details.rubella_serology " +
                ",ec_mother_details.serology_results ,ec_mother_details.mother_rubella " +
                ",ec_mother_details.mother_guardian_number as mother_phone_number" +
                ",ec_mother_details.second_phone_number as mother_second_phone_number" +
                ",ec_father_details.father_nationality ,ec_father_details.father_nationality_other " +
                ",ec_father_details.father_phone as father_phone_number,ec_child_details.inactive " +
                ",ec_child_details.lost_to_follow_up ,ec_child_details.relational_id ,ec_child_details.show_bcg_scar ," +
                "ec_child_details.show_bcg2_reminder ,ec_child_details.birth_registration_number " +
                ",ec_child_details.child_reg ,ec_child_details.place_of_birth ,ec_child_details.ga_at_birth " +
                ",ec_child_details.father_relational_id ,ec_child_details.sms_recipient " +
                ",ec_child_details.card_status ,ec_child_details.card_status_date ,mother.first_name                     " +
                "as mother_first_name,mother.last_name                      as mother_last_name,mother.dob                            " +
                "as mother_dob,father.first_name                     as father_first_name,father.last_name                      " +
                "as father_last_name,father.dob                            " +
                "as father_dob from ec_child_details join ec_mother_details on ec_child_details.relational_id = " +
                "ec_mother_details.base_entity_id left join ec_father_details on ec_child_details.father_relational_id = " +
                "ec_father_details.base_entity_id join ec_client on ec_client.base_entity_id = " +
                "ec_child_details.base_entity_id join ec_client mother on mother.base_entity_id = " +
                "ec_mother_details.base_entity_id left join ec_client father on father.base_entity_id = " +
                "ec_father_details.base_entity_id", appChildRegisterQueryProvider.mainRegisterQuery())
    }
}