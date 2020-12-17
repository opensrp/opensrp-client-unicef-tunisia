package org.smartregister.uniceftunisia.interactor;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;

import org.junit.Assert;
import org.junit.Test;

public class ChildJsonFormInteractorTest {

    @Test
    public void testChildFormInteractorMapContainsCustomEditTextWidget() {

        ChildFormInteractor childFormInteractor = (ChildFormInteractor) ChildFormInteractor.getInstance();
        Assert.assertNotNull(childFormInteractor);
        Assert.assertTrue(childFormInteractor instanceof JsonFormInteractor);

        Assert.assertNotNull(childFormInteractor.map);
        Assert.assertTrue(childFormInteractor.map.size() > 0);
        Assert.assertTrue(childFormInteractor.map.containsKey(JsonFormConstants.EDIT_TEXT));

    }
}
