package org.smartregister.uniceftunisia.interactor;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.junit.Assert;
import org.junit.Test;
import org.smartregister.child.widgets.ChildCheckboxTextFactory;
import org.smartregister.child.widgets.ChildEditTextFactory;
import org.smartregister.child.widgets.ChildSpinnerFactory;
import org.smartregister.uniceftunisia.widget.AdverseEffectDatePickerFactory;
import org.smartregister.uniceftunisia.widget.AppMultiSelectListFactory;

public class ChildJsonFormInteractorTest {

    @Test
    public void testChildFormInteractorMapContainsCustomEditTextWidget() {

        ChildFormInteractor childFormInteractor = ChildFormInteractor.getInstance();
        Assert.assertNotNull(childFormInteractor);

        Assert.assertNotNull(childFormInteractor.map);
        Assert.assertTrue(childFormInteractor.map.size() > 0);
        Assert.assertTrue(childFormInteractor.map.get(JsonFormConstants.EDIT_TEXT) instanceof ChildEditTextFactory);
        Assert.assertTrue(childFormInteractor.map.get(JsonFormConstants.DATE_PICKER) instanceof AdverseEffectDatePickerFactory);
        Assert.assertTrue(childFormInteractor.map.get(JsonFormConstants.CHECK_BOX) instanceof ChildCheckboxTextFactory);
        Assert.assertTrue(childFormInteractor.map.get(JsonFormConstants.SPINNER) instanceof ChildSpinnerFactory);
        Assert.assertTrue(childFormInteractor.map.get(JsonFormConstants.MULTI_SELECT_LIST) instanceof AppMultiSelectListFactory);

    }
}
