/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme.schooltimetabling.devui;

import groovy.namespace.QName;
import groovy.xml.XmlParser;
import io.quarkus.test.QuarkusDevModeTest;
import io.restassured.RestAssured;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import groovy.util.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;

public class OptaPlannerDevUITest {
    @RegisterExtension
    static final QuarkusDevModeTest config = new QuarkusDevModeTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addPackages(true, "org.acme.schooltimetabling"));

    static final String OPTAPLANNER_DEV_UI_BASE_URL = "/q/dev/org.optaplanner.optaplanner-quarkus/";

    public static String getPage(String pageName) {
        return OPTAPLANNER_DEV_UI_BASE_URL + pageName;
    }

    @Test
    public void testSolverConfigPage() throws ParserConfigurationException, SAXException, IOException {
        String body = RestAssured.get(getPage("solverConfig"))
                .then()
                .extract()
                .body()
                .asPrettyString();
        XmlParser xmlParser = new XmlParser();
        Node node = xmlParser.parseText(body);
        String solverConfig = ((Node) (node.getAt(QName.valueOf("body"))
                .getAt(QName.valueOf("div"))
                .getAt(QName.valueOf("div"))
                .get(0))).text();
        assertThat(solverConfig).isEqualToIgnoringWhitespace(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!--Properties that can be set at runtime are not included-->"
                + "<solver>"
                +   "<solutionClass>org.acme.schooltimetabling.domain.TimeTable</solutionClass>"
                +   "<entityClass>org.acme.schooltimetabling.domain.Lesson</entityClass>"
                +   "<domainAccessType>GIZMO</domainAccessType>"
                +   "<scoreDirectorFactory>"
                +     "<constraintProviderClass>org.acme.schooltimetabling.solver.TimeTableConstraintProvider</constraintProviderClass>"
                +   "</scoreDirectorFactory>"
                + "</solver>"
        );
    }

    @Test
    public void testModelPage() throws ParserConfigurationException, SAXException, IOException {
        String body = RestAssured.get(getPage("model"))
                .then()
                .extract()
                .body()
                .asPrettyString();
        XmlParser xmlParser = new XmlParser();
        Node node = xmlParser.parseText(body);
        String model = ((Node) (node.getAt(QName.valueOf("body"))
                .getAt(QName.valueOf("div"))
                .getAt(QName.valueOf("div"))
                .get(0))).toString();
        assertThat(model).contains("value=[Solution: org.acme.schooltimetabling.domain.TimeTable]");
        assertThat(model).contains("value=[Entity: org.acme.schooltimetabling.domain.Lesson]");
        assertThat(model).contains("value=[Genuine Variables]]]]]], tbody[attributes={}; value=["
                + "tr[attributes={}; value=[td[attributes={colspan=1, rowspan=1}; value=[room]]]], "
                + "tr[attributes={}; value=[td[attributes={colspan=1, rowspan=1}; value=[timeslot]]]]]]]]");
        assertThat(model).contains("value=[th[attributes={colspan=1, rowspan=1, scope=col}; value=[Shadow Variables]]]]]], tbody[attributes={}; value=[]]]");
    }

    @Test
    public void testConstraintsPage() throws ParserConfigurationException, SAXException, IOException {
        String body = RestAssured.get(getPage("constraints"))
                .then()
                .extract()
                .body()
                .asPrettyString();
        XmlParser xmlParser = new XmlParser();
        Node node = xmlParser.parseText(body);
        String constraints = ((Node) (node.getAt(QName.valueOf("body"))
                .getAt(QName.valueOf("div"))
                .getAt(QName.valueOf("table"))
                .get(0))).text();
        assertThat(constraints).contains("org.acme.schooltimetabling.domain/Room conflict");
        assertThat(constraints).contains("org.acme.schooltimetabling.domain/Teacher conflict");
        assertThat(constraints).contains("org.acme.schooltimetabling.domain/Student group conflict");
        assertThat(constraints).contains("org.acme.schooltimetabling.domain/Teacher room stability");
        assertThat(constraints).contains("org.acme.schooltimetabling.domain/Teacher time efficiency");
        assertThat(constraints).contains("org.acme.schooltimetabling.domain/Student group subject variety");
    }
}
