/**
 ISAcreator is a component of the ISA software suite (http://www.isa-tools.org)

 License:
 ISAcreator is licensed under the Common Public Attribution License version 1.0 (CPAL)

 EXHIBIT A. CPAL version 1.0
 The contents of this file are subject to the CPAL version 1.0 (the License);
 you may not use this file except in compliance with the License. You may obtain a
 copy of the License at http://isa-tools.org/licenses/ISAcreator-license.html.
 The License is based on the Mozilla Public License version 1.1 but Sections
 14 and 15 have been added to cover use of software over a computer network and
 provide for limited attribution for the Original Developer. In addition, Exhibit
 A has been modified to be consistent with Exhibit B.

 Software distributed under the License is distributed on an AS IS basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License.

 The Original Code is ISAcreator.
 The Original Developer is the Initial Developer. The Initial Developer of the
 Original Code is the ISA Team (Eamonn Maguire, eamonnmag@gmail.com;
 Philippe Rocca-Serra, proccaserra@gmail.com; Susanna-Assunta Sansone, sa.sanson@gmail.com;
 http://www.isa-tools.org). All portions of the code written by the ISA Team are
 Copyright (c) 2007-2011 ISA Team. All Rights Reserved.

 EXHIBIT B. Attribution Information
 Attribution Copyright Notice: Copyright (c) 2008-2011 ISA Team
 Attribution Phrase: Developed by the ISA Team
 Attribution URL: http://www.isa-tools.org
 Graphic Image provided in the Covered Code as file: http://isa-tools.org/licenses/icons/poweredByISAtools.png
 Display of Attribution Information is required in Larger Works which are defined in the CPAL as a work which combines Covered Code or portions thereof with code not governed by the terms of the CPAL.

 Sponsors:
 The ISA Team and the ISA software suite have been funded by the EU Carcinogenomics project (http://www.carcinogenomics.eu), the UK BBSRC (http://www.bbsrc.ac.uk), the UK NERC-NEBC (http://nebc.nerc.ac.uk) and in part by the EU NuGO consortium (http://www.nugo.org/everyone).
 */

package org.isatools.isacreator.settings;

import org.isatools.isacreator.common.UIHelper;
import org.isatools.isacreator.effects.borders.RoundedBorder;
import org.isatools.isacreator.effects.components.RoundedJPasswordField;
import org.isatools.isacreator.effects.components.RoundedJTextField;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

/**
 * @author
 * @date
 */


public class SciGraphSettings extends SettingsScreen {

    private JPanel scigraphDetails;

    private JPanel hostNamePanel;
    private JPanel portNumberPanel;
    private JPanel ontologyPanel;
    private JPanel parametersPanel;

    // general fields
    private JCheckBox useSciGraph;
    private JTextField hostName;
    private JTextField portNumber;
    private JTextField ontology;

    private JTextField limit;
    private JCheckBox synonyms;
    private JCheckBox abbreviations;
    private JCheckBox acronyms;
    private JCheckBox deprecated;

    public SciGraphSettings(Properties settings) {

        this.settings = settings;
        setLayout(new BorderLayout());
        setOpaque(false);
        add(createProxyConfigPanel(), BorderLayout.NORTH);
        setBorder(new TitledBorder(
                new RoundedBorder(UIHelper.LIGHT_GREEN_COLOR, 9),
                "scigraph configuration", TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, UIHelper.VER_12_BOLD,
                UIHelper.GREY_COLOR));

        updatePanelIsActive(useSciGraph.isSelected(),
                hostNamePanel, portNumberPanel);
    }

    // to contain section

    private JPanel createProxyConfigPanel() {

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.setOpaque(false);

        useSciGraph = new JCheckBox("use scigraph", Boolean.valueOf(settings.getProperty("scigraph.use")));
        useSciGraph.setHorizontalAlignment(SwingConstants.LEFT);
        UIHelper.renderComponent(useSciGraph, UIHelper.VER_12_BOLD, UIHelper.GREY_COLOR, false);

        useSciGraph.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                updatePanelIsActive(useSciGraph.isSelected(),
                        hostNamePanel, portNumberPanel);
            }
        });

        container.add(UIHelper.wrapComponentInPanel(useSciGraph));

        scigraphDetails = new JPanel();
        scigraphDetails.setLayout(new BoxLayout(scigraphDetails, BoxLayout.PAGE_AXIS));

        // add host name and port number
        hostNamePanel = new JPanel(new GridLayout(1, 3));
        hostNamePanel.setOpaque(false);
        hostNamePanel.add(Box.createHorizontalStrut(10));
        hostNamePanel.add(UIHelper.createLabel("host", UIHelper.VER_12_BOLD, UIHelper.GREY_COLOR));
        hostName = new RoundedJTextField(10);
        hostName.setText(settings.getProperty("scigraph.host"));
        UIHelper.renderComponent(hostName, UIHelper.VER_12_PLAIN, UIHelper.DARK_GREEN_COLOR, false);
        hostNamePanel.add(hostName);

        scigraphDetails.add(hostNamePanel);

        portNumberPanel = new JPanel(new GridLayout(1, 3));
        portNumberPanel.setOpaque(false);
        portNumberPanel.add(Box.createHorizontalStrut(10));
        portNumberPanel.add(UIHelper.createLabel("port", UIHelper.VER_12_BOLD, UIHelper.GREY_COLOR));
        portNumber = new RoundedJTextField(10);
        portNumber.setText(settings.getProperty("scigraph.port"));
        UIHelper.renderComponent(portNumber, UIHelper.VER_12_PLAIN, UIHelper.GREY_COLOR, false);
        portNumberPanel.add(portNumber);

        scigraphDetails.add(portNumberPanel);

        ontologyPanel = new JPanel(new GridLayout(1, 3));
        ontologyPanel.setOpaque(false);
        ontologyPanel.add(Box.createHorizontalStrut(10));
        ontologyPanel.add(UIHelper.createLabel("default ontology", UIHelper.VER_12_BOLD, UIHelper.GREY_COLOR));
        ontology = new RoundedJTextField(10);
        ontology.setText(settings.getProperty("scigraph.ontology.default"));
        UIHelper.renderComponent(ontology, UIHelper.VER_12_PLAIN, UIHelper.GREY_COLOR, false);
        ontologyPanel.add(ontology);

        scigraphDetails.add(ontologyPanel);

        parametersPanel = new JPanel(new GridLayout(5, 3));
        parametersPanel.setOpaque(false);
        parametersPanel.add(Box.createHorizontalStrut(10));
        parametersPanel.add(UIHelper.createLabel("limit", UIHelper.VER_12_BOLD, UIHelper.GREY_COLOR));
        limit = new RoundedJTextField(10);
        limit.setText(settings.getProperty("scigraph.search.limit"));
        UIHelper.renderComponent(limit, UIHelper.VER_12_PLAIN, UIHelper.DARK_GREEN_COLOR, false);
        parametersPanel.add(limit);
        parametersPanel.add(Box.createHorizontalStrut(10));
        parametersPanel.add(Box.createHorizontalStrut(10));
        synonyms = new JCheckBox("synonyms", Boolean.valueOf(settings.getProperty("scigraph.search.synonyms")));
        synonyms.setHorizontalAlignment(SwingConstants.LEFT);
        UIHelper.renderComponent(synonyms, UIHelper.VER_12_BOLD, UIHelper.GREY_COLOR, false);
        parametersPanel.add(synonyms);
        parametersPanel.add(Box.createHorizontalStrut(10));
        parametersPanel.add(Box.createHorizontalStrut(10));
        abbreviations = new JCheckBox("abbreviations", Boolean.valueOf(settings.getProperty("scigraph.search.abbreviations")));
        abbreviations.setHorizontalAlignment(SwingConstants.LEFT);
        UIHelper.renderComponent(abbreviations, UIHelper.VER_12_BOLD, UIHelper.GREY_COLOR, false);
        parametersPanel.add(abbreviations);
        parametersPanel.add(Box.createHorizontalStrut(10));
        parametersPanel.add(Box.createHorizontalStrut(10));
        acronyms = new JCheckBox("acronyms", Boolean.valueOf(settings.getProperty("scigraph.search.acronyms")));
        acronyms.setHorizontalAlignment(SwingConstants.LEFT);
        UIHelper.renderComponent(acronyms, UIHelper.VER_12_BOLD, UIHelper.GREY_COLOR, false);
        parametersPanel.add(acronyms);
        parametersPanel.add(Box.createHorizontalStrut(10));
        parametersPanel.add(Box.createHorizontalStrut(10));
        deprecated = new JCheckBox("deprecated", Boolean.valueOf(settings.getProperty("scigraph.search.deprecated")));
        deprecated.setHorizontalAlignment(SwingConstants.LEFT);
        UIHelper.renderComponent(deprecated, UIHelper.VER_12_BOLD, UIHelper.GREY_COLOR, false);
        parametersPanel.add(deprecated);

        scigraphDetails.add(parametersPanel);

        container.add(scigraphDetails);

        return container;
    }

    private void updatePanelIsActive(boolean active, Container... panels) {
        for (Container p : panels) {
            disableEnableJPanelContents(p, active);
        }
        revalidate();
        repaint();
    }

    private void disableEnableJPanelContents(Container panel, boolean isEnabled) {
        for (Component c : panel.getComponents()) {
            c.setEnabled(isEnabled);
        }

    }

    public boolean updateSettings() {
        // set new properties...
        try {
            settings.setProperty("scigraph.use", String.valueOf(useSciGraph.isSelected()));
            settings.setProperty("scigraph.host", hostName.getText());
            settings.setProperty("scigraph.port", portNumber.getText());
            settings.setProperty("scigraph.ontology.default", ontology.getText());
            settings.setProperty("scigraph.search.limit", limit.getText());
            settings.setProperty("scigraph.search.synonyms", String.valueOf(synonyms.isSelected()));
            settings.setProperty("scigraph.search.abbreviations", String.valueOf(abbreviations.isSelected()));
            settings.setProperty("scigraph.search.acronyms", String.valueOf(acronyms.isSelected()));
            settings.setProperty("scigraph.search.deprecated", String.valueOf(deprecated.isSelected()));
            return true;
        } catch (Exception e) {
            log.error("Problem occurred when trying to update scigraph settings.");
            return false;
        }
    }

    protected void performImportLogic() {

    }

    protected void performExportLogic() {

    }

    protected void performDeletionLogic() {

    }


}
