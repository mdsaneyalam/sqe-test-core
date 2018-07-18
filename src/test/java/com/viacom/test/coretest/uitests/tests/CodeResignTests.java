package com.viacom.test.coretest.uitests.tests;

import com.viacom.test.coretest.common.BaseTest;
import com.viacom.test.coretest.common.util.props.IProps.GroupProps;
import com.viacom.test.coretest.uitests.support.DefaultCapabilityFactory;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ru.yandex.qatools.allure.annotations.Features;

public class CodeResignTests extends BaseTest {

	DefaultCapabilityFactory defaultCaps = null;
	
	@BeforeMethod(alwaysRun = true)
    public void setupTest() {
		defaultCaps = new DefaultCapabilityFactory();
    }
	
	@Test(groups = { GroupProps.IOS })
    @Features(GroupProps.IOS)
	public void codeResigniOSAppTest() {
    	
    	
	}
    
}
