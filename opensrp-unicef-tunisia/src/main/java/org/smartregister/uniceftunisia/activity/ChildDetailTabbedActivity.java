package org.smartregister.uniceftunisia.activity;

import static org.smartregister.clientandeventmodel.DateUtil.getDateFromString;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.vijay.jsonwizard.activities.JsonWizardFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.child.activity.BaseChildDetailTabbedActivity;
import org.smartregister.child.fragment.StatusEditDialogFragment;
import org.smartregister.child.presenter.BaseChildDetailsPresenter.CardStatus;
import org.smartregister.child.task.LoadAsyncTask;
import org.smartregister.child.util.ChildDbUtils;
import org.smartregister.child.util.ChildJsonFormUtils;
import org.smartregister.child.util.Constants;
import org.smartregister.uniceftunisia.R;
import org.smartregister.uniceftunisia.fragment.ChildRegistrationDataFragment;
import org.smartregister.uniceftunisia.util.AppConstants;
import org.smartregister.uniceftunisia.util.AppJsonFormUtils;
import org.smartregister.uniceftunisia.util.AppUtils;
import org.smartregister.uniceftunisia.util.VaccineUtils;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by ndegwamartin on 06/03/2019.
 */
public class ChildDetailTabbedActivity extends BaseChildDetailTabbedActivity {
    private final static List<String> nonEditableFields = Arrays.asList("Sex", "zeir_id", "mother_rubella", "protected_at_birth");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getChildUnderFiveFragment().showRecurringServices(false);
    }

    @Override
    public void onUniqueIdFetched(Triple<String, Map<String, String>, String> triple, String s) {
        //Overridden - not required
    }

    @Override
    public void onNoUniqueId() {
        //Overridden - not required
    }

    public ChildRegistrationDataFragment getChildRegistrationDataFragment() {
        return new ChildRegistrationDataFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        overflow.findItem(R.id.register_card).setVisible(false);
        overflow.findItem(R.id.write_to_card).setVisible(false);
        overflow.findItem(R.id.recurring_services_data).setVisible(false);
        overflow.findItem(R.id.record_dynamic_vaccines).setVisible(true);

        MenuItem lostCardMenu = overflow.findItem(R.id.report_lost_card);
        lostCardMenu.setVisible(true);

        String cardStatus = childDetails.getDetails().get(AppConstants.KEY.CARD_STATUS);
        String cardStatusDate = childDetails.getDetails().get(AppConstants.KEY.CARD_STATUS_DATE);

        if (CardStatus.needs_card.name().equalsIgnoreCase(cardStatus) &&
                StringUtils.isNotBlank(cardStatusDate)) {
            lostCardMenu.setEnabled(false);
            lostCardMenu.setTitle(getString(org.smartregister.child.R.string.card_ordered_with_date,
                    ddMmYyyyDateFormat.format(getDateFromString(cardStatusDate))));
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        detailsMap = ChildDbUtils.fetchChildDetails(getChildDetails().entityId());
        detailsMap.putAll(ChildDbUtils.fetchChildFirstGrowthAndMonitoring(getChildDetails().entityId()));

        switch (item.getItemId()) {
            case R.id.registration_data:
                String populatedForm = AppJsonFormUtils.updateJsonFormWithClientDetails(this, detailsMap, nonEditableFields);
                startFormActivity(populatedForm);
                // User chose the "Settings" item, show the app settings UI...
                return true;
            case R.id.immunization_data:
                if (viewPager.getCurrentItem() != 1) {
                    viewPager.setCurrentItem(1);
                }
                Utils.startAsyncTask(
                        new LoadAsyncTask(org.smartregister.child.enums.Status.EDIT_VACCINE, detailsMap, getChildDetails(), this, getChildDataFragment(), getChildUnderFiveFragment(), getOverflow()),
                        null);
                saveButton.setVisibility(View.VISIBLE);
                for (int i = 0; i < overflow.size(); i++) {
                    overflow.getItem(i).setVisible(false);
                }
                return true;

            case R.id.recurring_services_data:
                if (viewPager.getCurrentItem() != 1) {
                    viewPager.setCurrentItem(1);
                }
                Utils.startAsyncTask(
                        new LoadAsyncTask(org.smartregister.child.enums.Status.EDIT_SERVICE, detailsMap, getChildDetails(), this, getChildDataFragment(), getChildUnderFiveFragment(), getOverflow()),
                        null);
                saveButton.setVisibility(View.VISIBLE);
                for (int i = 0; i < overflow.size(); i++) {
                    overflow.getItem(i).setVisible(false);
                }
                return true;
            case R.id.weight_data:
                if (viewPager.getCurrentItem() != 1) {
                    viewPager.setCurrentItem(1);
                }
                Utils.startAsyncTask(new LoadAsyncTask(org.smartregister.child.enums.Status.EDIT_GROWTH, detailsMap, getChildDetails(), this, getChildDataFragment(), getChildUnderFiveFragment(), getOverflow()), null);
                saveButton.setVisibility(View.VISIBLE);
                for (int i = 0; i < overflow.size(); i++) {
                    overflow.getItem(i).setVisible(false);
                }
                return true;

            case R.id.report_deceased:
                String reportDeceasedMetadata = getReportDeceasedMetadata();
                startFormActivity(reportDeceasedMetadata);
                return true;
            case R.id.change_status:
                FragmentTransaction ft = this.getFragmentManager().beginTransaction();
                android.app.Fragment prev = this.getFragmentManager().findFragmentByTag(DIALOG_TAG);
                if (prev != null) {
                    ft.remove(prev);
                }
                StatusEditDialogFragment.newInstance(detailsMap).show(ft, DIALOG_TAG);
                return true;
            case R.id.report_adverse_event:
                return launchAdverseEventForm();
            case R.id.record_dynamic_vaccines:
                if (getExtraChildVaccines().size() < 10) {
                    launchDynamicVaccinesForm(AppConstants.JsonForm.DYNAMIC_VACCINES, Constants.KEY.PRIVATE_SECTOR_VACCINE);
                } else {
                    Utils.showToast(this, getString(R.string.maximum_extra_vaccines_reached));
                }
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void launchDynamicVaccinesForm(String dynamicVaccinesForm, String multiSelectFieldName) {
        String locationId = Utils.getAllSharedPreferences().fetchDefaultLocalityId(Utils.getAllSharedPreferences().fetchRegisteredANM());
        try {
            JSONObject form = FormUtils.getInstance(getContext()).getFormJson(dynamicVaccinesForm);
            form.put(Constants.KEY.ENTITY_ID, childDetails.getCaseId());
            if (form.has(ChildJsonFormUtils.METADATA)) {
                form.getJSONObject(ChildJsonFormUtils.METADATA).put(ChildJsonFormUtils.ENCOUNTER_LOCATION, locationId);
            }
            JSONObject stepOne = form.getJSONObject(ChildJsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(ChildJsonFormUtils.FIELDS);

            //Limit the size of multi select widget field size to the number of extra vaccines
            JSONObject multiSelectField = ChildJsonFormUtils.getFieldJSONObject(jsonArray, multiSelectFieldName);
            multiSelectField.put(AppConstants.KEY.MAX_SELECTABLE, 10 - getExtraChildVaccines().size());

            form.put(Constants.KEY.DYNAMIC_FIELD, multiSelectFieldName);
            form.put(Constants.KEY.ENTITY_ID, childDetails.entityId());

            startFormActivity(form.toString());
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected void navigateToRegisterActivity() {
        Intent intent = new Intent(this, ChildRegisterActivity.class);
        intent.putExtra(AllConstants.INTENT_KEY.IS_REMOTE_LOGIN, false);
        startActivity(intent);
        finish();
    }

    @Override
    public void startFormActivity(String formData) {
        try {
            Intent intent;
            Form form = new Form();

            JSONObject formJson = new JSONObject(formData);
            if (formJson.has(JsonFormConstants.ENCOUNTER_TYPE) &&
                    formJson.getString(JsonFormConstants.ENCOUNTER_TYPE).equalsIgnoreCase(Constants.EventType.AEFI)) {
                form.setWizard(true);
                form.setName(getString(R.string.adverse_effects));
                form.setHideSaveLabel(true);
                form.setNextLabel(getString(R.string.next));
                form.setPreviousLabel(getString(R.string.previous));
                form.setSaveLabel(getString(R.string.save));
                form.setActionBarBackground(R.color.actionbar);
                form.setNavigationBackground(R.color.primary_dark);
                intent = new Intent(this, JsonWizardFormActivity.class);
                formData = obtainUpdatedForm(formJson);
            } else {
                form.setWizard(false);
                form.setHideSaveLabel(true);
                form.setNextLabel("");
                intent = new Intent(this, org.smartregister.child.util.Utils.metadata().childFormActivity);
            }

            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.JSON, formData);
            intent.putExtra(JsonFormConstants.PERFORM_FORM_TRANSLATION, true);
            startActivityForResult(intent, REQUEST_CODE_GET_JSON);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    private String obtainUpdatedForm(JSONObject formJson) throws JSONException {
        JSONArray fields = JsonFormUtils.fields(formJson);
        for (int i = 0; i < fields.length(); i++) {
            JSONObject field = fields.getJSONObject(i);
            if (field != null && !field.getString(JsonFormConstants.KEY).equalsIgnoreCase(AppConstants.KEY.EXPIRY)
                    && !field.getString(JsonFormConstants.KEY).equalsIgnoreCase(AppConstants.KEY.THHINNER_EXPIRY)) {
                if (field.getString(JsonFormConstants.TYPE).equalsIgnoreCase(JsonFormConstants.DATE_PICKER)
                        && !childDetails.getDetails().isEmpty() && childDetails.getDetails().containsKey(AppConstants.KEY.DOB)) {
                    Date date = Utils.dobStringToDate(childDetails.getDetails().get(AppConstants.KEY.DOB));
                    field.put(JsonFormConstants.MIN_DATE, new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date));
                    field.put(JsonFormConstants.MAX_DATE, AppConstants.KEY.TODAY);
                }
            }
        }
        return formJson.toString();
    }

    @Override
    protected String getReportDeceasedMetadata() {
        try {
            JSONObject form = FormUtils.getInstance(getApplicationContext()).getFormJson("report_deceased");
            if (form != null) {
                //inject zeir id into the form
                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Date_of_Death")) {
                        SimpleDateFormat simpleDateFormat =
                                new SimpleDateFormat(com.vijay.jsonwizard.utils.FormUtils.NATIIVE_FORM_DATE_FORMAT_PATTERN,
                                        Locale.ENGLISH);
                        String dobString = Utils.getValue(childDetails.getColumnmaps(), "dob", true);
                        Date dob = Utils.dobStringToDate(dobString);
                        if (dob != null) {
                            jsonObject.put("min_date", simpleDateFormat.format(dob));
                        }
                        break;
                    }
                }
            }

            return form == null ? null : form.toString();

        } catch (Exception e) {
            Timber.e(e);
        }
        return "";
    }

    @Override
    public void notifyLostCardReported(String orderDate) {
        super.notifyLostCardReported(orderDate);
        AppUtils.createClientCardReceivedEvent(childDetails.getCaseId(), CardStatus.needs_card, orderDate);
    }

    @Override
    public void onRegistrationSaved(boolean isEdit) {
        super.onRegistrationSaved(isEdit);
        if (isEdit) {
            VaccineUtils.refreshImmunizationSchedules(childDetails.getCaseId());
        }
    }

    @Override
    public String constructChildName() {
        return super.constructChildName();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (data != null) {
                String jsonString = data.getStringExtra(JsonFormConstants.JSON_FORM_KEY.JSON);
                Timber.d(jsonString);
                if (jsonString != null) {
                    JSONObject form = new JSONObject(jsonString);
                    String encounterType = form.getString(ChildJsonFormUtils.ENCOUNTER_TYPE);
                    if (encounterType.equalsIgnoreCase(Constants.EventType.UPDATE_BITRH_REGISTRATION)) {
                        String jsonForm = AppUtils.validateSpinnerValue(jsonString);
                        data.putExtra(JsonFormConstants.JSON_FORM_KEY.JSON, jsonForm);
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
