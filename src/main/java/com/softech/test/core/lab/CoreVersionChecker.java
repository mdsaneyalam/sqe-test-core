package com.softech.test.core.lab;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.softech.test.core.util.Logger;

public class CoreVersionChecker {

	public static void checkCoreVersion() {
		if (!GridManager.isEC2Agent()) {
			return;
		}

		String userCoreVersion = null;

		// get the current supported version list
		List<String> supportedVersions = Arrays.asList(System.getenv("SUPPORTED_CORE_VERSIONS").split(","));

		// get the user's core build
		try {

			File coreFile = new File(System.getProperty("user.dir") + File.separator + "pom.xml");
			if (coreFile.exists()) {
				// get the report file xml
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				dbf.setIgnoringElementContentWhitespace(true);
				Document document = dbf.newDocumentBuilder().parse(coreFile);

				// construct xpath query for each pending node
				XPathFactory xpf = XPathFactory.newInstance();
				XPath xpath = xpf.newXPath();
				XPathExpression xpathE = xpath.compile("//artifactId[text()='mqe-test-core']/../version");

				XPathExpression expression = xpathE;

				NodeList results = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
				for (int i = 0; i < results.getLength(); i++) {
					Node node = results.item(i);
					userCoreVersion = node.getTextContent();
				}
			}
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to retrieve project core version.");
			e.printStackTrace();
		}

		if (userCoreVersion != null) {
			if (userCoreVersion.toLowerCase().contains("debug")) {
				Logger.logConsoleMessage(
						"The project is running a debug version of mqe-test-core that is not officially "
								+ "supported. Please consider updating to an official release version.");
			} else {
				if (!supportedVersions.contains(userCoreVersion)) {
					GlobalAbort.terminateTestSuite("The project is running mqe-test-core version '" + userCoreVersion
							+ "' which is not supported! Supported versions include: "
							+ System.getenv("SUPPORTED_CORE_VERSIONS")
							+ ". Please update your project to one of these supported versions ASAP!");
				}
			}
		}
	}

}
