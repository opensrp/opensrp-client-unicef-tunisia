package org.smartregister.uniceftunisia.activity;

import android.app.Activity;
import android.content.Intent;

import com.vijay.jsonwizard.activities.JsonWizardFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.util.ReflectionHelpers;
import org.skyscreamer.jsonassert.JSONAssert;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.uniceftunisia.BaseActivityUnitTest;

import java.util.HashMap;
import java.util.Map;

public class ChildDetailTabbedActivityTest extends BaseActivityUnitTest {

    private ChildDetailTabbedActivity  childDetailTabbedActivity;

    private ActivityController<ChildDetailTabbedActivity> controller;

    @Before
    public void setUp(){
        controller = Robolectric.buildActivity(ChildDetailTabbedActivity.class);
        childDetailTabbedActivity = controller.get();
    }

    @Override
    protected Activity getActivity() {
        return childDetailTabbedActivity;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected ActivityController getActivityController() {
        return controller;
    }

    @Test
    public void constructChildNameTest() {
        Map<String, String> columnMaps = new HashMap<>();
        columnMaps.put("first_name", "first");
        columnMaps.put("last_name", "last");

        CommonPersonObjectClient commonFtsObject = Mockito.mock(CommonPersonObjectClient.class);
        Mockito.doReturn(columnMaps).when(commonFtsObject).getColumnmaps();
        ReflectionHelpers.setField(childDetailTabbedActivity, "childDetails", commonFtsObject);

        Assert.assertEquals("First Last", childDetailTabbedActivity.constructChildName());

    }

    @Test
    public void testOnActivityResult() throws JSONException {
        String initialJson = "{\n" +
                "   \"count\":\"1\",\n" +
                "   \"encounter_type\":\"Update Birth Registration\",\n" +
                "   \"mother\":{\n" +
                "      \"encounter_type\":\"New Woman Registration\"\n" +
                "   },\n" +
                "   \"entity_id\":\"d2812b33-2abe-482a-8838-c89fae8ac7b6\",\n" +
                "   \"relational_id\":\"999891ad-1f4a-49c1-9186-029e1ce66509\",\n" +
                "     \"step1\":{\n" +
                "      \"title\":\"Birth Registration\",\n" +
                "      \"fields\":[\n" +
                "         {\n" +
                "            \"key\":\"mother_tdv_doses\",\n" +
                "            \"openmrs_entity_parent\":\"\",\n" +
                "            \"openmrs_entity\":\"person_attribute\",\n" +
                "            \"openmrs_entity_id\":\"mother_tdv_doses\",\n" +
                "            \"type\":\"spinner\",\n" +
                "            \"hint\":\"How many doses of Td vaccine did the mother receive during pregnancy?\",\n" +
                "            \"value\":\"How many doses of Td vaccine did the mother receive during pregnancy?\",\n" +
                "            \"read_only\":false\n" +
                "         }\n" +
                "      ]\n" +
                "   },\n" +
                "   \"properties_file_name\":\"child_enrollment\",\n" +
                "   \"details\":{\n" +
                "      \"appVersionName\":\"2.0.5-SNAPSHOT\",\n" +
                "      \"formVersion\":\"\"\n" +
                "   }\n" +
                "}";
        Intent data = new Intent();
        data.putExtra(JsonFormConstants.JSON_FORM_KEY.JSON, initialJson);

        childDetailTabbedActivity.onActivityResult(100, 200, data);
        String finalJson = "{\n" +
                "   \"count\":\"1\",\n" +
                "   \"encounter_type\":\"Update Birth Registration\",\n" +
                "   \"mother\":{\n" +
                "      \"encounter_type\":\"New Woman Registration\"\n" +
                "   },\n" +
                "   \"entity_id\":\"d2812b33-2abe-482a-8838-c89fae8ac7b6\",\n" +
                "   \"relational_id\":\"999891ad-1f4a-49c1-9186-029e1ce66509\",\n" +
                "     \"step1\":{\n" +
                "      \"title\":\"Birth Registration\",\n" +
                "      \"fields\":[\n" +
                "         {\n" +
                "            \"key\":\"mother_tdv_doses\",\n" +
                "            \"openmrs_entity_parent\":\"\",\n" +
                "            \"openmrs_entity\":\"person_attribute\",\n" +
                "            \"openmrs_entity_id\":\"mother_tdv_doses\",\n" +
                "            \"openmrs_data_type\":\"text\",\n" +
                "            \"type\":\"spinner\",\n" +
                "            \"hint\":\"How many doses of Td vaccine did the mother receive during pregnancy?\",\n" +
                "            \"read_only\":false\n" +
                "         }\n" +
                "      ]\n" +
                "   },\n" +
                "   \"properties_file_name\":\"child_enrollment\",\n" +
                "   \"details\":{\n" +
                "      \"appVersionName\":\"2.0.5-SNAPSHOT\",\n" +
                "      \"formVersion\":\"\"\n" +
                "   }\n" +
                "}";

        JSONAssert.assertEquals(data.getStringExtra(JsonFormConstants.JSON_FORM_KEY.JSON), finalJson, false);
    }

    @Test
    public void testStartFormActivity() throws JSONException {
        String finalJson = "{\n" +
                "   \"count\":\"1\",\n" +
                "   \"encounter_type\":\"Update Birth Registration\",\n" +
                "   \"mother\":{\n" +
                "      \"encounter_type\":\"New Woman Registration\"\n" +
                "   },\n" +
                "   \"entity_id\":\"d2812b33-2abe-482a-8838-c89fae8ac7b6\",\n" +
                "   \"relational_id\":\"999891ad-1f4a-49c1-9186-029e1ce66509\",\n" +
                "     \"step1\":{\n" +
                "      \"title\":\"Birth Registration\",\n" +
                "      \"fields\":[\n" +
                "         {\n" +
                "            \"key\":\"mother_tdv_doses\",\n" +
                "            \"openmrs_entity_parent\":\"\",\n" +
                "            \"openmrs_entity\":\"person_attribute\",\n" +
                "            \"openmrs_entity_id\":\"mother_tdv_doses\",\n" +
                "            \"openmrs_data_type\":\"text\",\n" +
                "            \"type\":\"spinner\",\n" +
                "            \"hint\":\"How many doses of Td vaccine did the mother receive during pregnancy?\",\n" +
                "            \"read_only\":false\n" +
                "         }\n" +
                "      ]\n" +
                "   },\n" +
                "   \"properties_file_name\":\"child_enrollment\",\n" +
                "   \"details\":{\n" +
                "      \"appVersionName\":\"2.0.5-SNAPSHOT\",\n" +
                "      \"formVersion\":\"\"\n" +
                "   }\n" +
                "}";
        ChildDetailTabbedActivity spyActivity = Mockito.spy(childDetailTabbedActivity);
        spyActivity.startFormActivity(finalJson);
        assertActivityStarted(spyActivity, new ChildFormActivity());

        String aefiJson = "{\n" +
                "   \"count\":\"1\",\n" +
                "   \"encounter_type\":\"AEFI\",\n" +
                "   \"mother\":{\n" +
                "      \"encounter_type\":\"New Woman Registration\"\n" +
                "   },\n" +
                "   \"entity_id\":\"d2812b33-2abe-482a-8838-c89fae8ac7b6\",\n" +
                "   \"relational_id\":\"999891ad-1f4a-49c1-9186-029e1ce66509\",\n" +
                "     \"step1\":{\n" +
                "      \"title\":\"Birth Registration\",\n" +
                "      \"fields\":[\n" +
                "         {\n" +
                "            \"key\":\"mother_tdv_doses\",\n" +
                "            \"openmrs_entity_parent\":\"\",\n" +
                "            \"openmrs_entity\":\"person_attribute\",\n" +
                "            \"openmrs_entity_id\":\"mother_tdv_doses\",\n" +
                "            \"openmrs_data_type\":\"text\",\n" +
                "            \"type\":\"spinner\",\n" +
                "            \"hint\":\"How many doses of Td vaccine did the mother receive during pregnancy?\",\n" +
                "            \"read_only\":false\n" +
                "         }\n" +
                "      ]\n" +
                "   },\n" +
                "   \"properties_file_name\":\"child_enrollment\",\n" +
                "   \"details\":{\n" +
                "      \"appVersionName\":\"2.0.5-SNAPSHOT\",\n" +
                "      \"formVersion\":\"\"\n" +
                "   }\n" +
                "}";

        spyActivity.startFormActivity(aefiJson);
        assertActivityStarted(spyActivity, new JsonWizardFormActivity());
    }

    @Test
    public void testNavigateToRegisterActivity() {
        ChildDetailTabbedActivity spyActivity = Mockito.spy(childDetailTabbedActivity);
        spyActivity.navigateToRegisterActivity();
        assertActivityStarted(spyActivity, new ChildRegisterActivity());
    }
}
