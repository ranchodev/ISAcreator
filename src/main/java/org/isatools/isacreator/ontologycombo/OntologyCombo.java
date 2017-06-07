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

package org.isatools.isacreator.ontologycombo;

import org.apache.commons.lang.StringUtils;
import org.isatools.isacreator.common.UIHelper;
import org.isatools.isacreator.configuration.RecommendedOntology;
import org.isatools.isacreator.ontologymanager.OntologyManager;
import org.isatools.isacreator.ontologymanager.SciGraph4Client;
import org.isatools.isacreator.ontologymanager.common.OntologyTerm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OntologyCombo filters data (ontologies and terms) on input.
 *
 */
public class OntologyCombo extends JComboBox {

    private Map<String, RecommendedOntology> recommendedOntologyMap;

    private boolean layingOut = false;

    @Override
    public void doLayout() {
        try{
            layingOut = true;
            super.doLayout();
        }finally{
            layingOut = false;
        }
    }

    @Override
    public Dimension getSize() {
        Dimension dim = super.getSize();
        if(!layingOut)
            dim.width = Math.max(dim.width, getPreferredSize().width);
        return dim;
    }

    /**
     *  ComboBox for ontologies/terms search
     *
     * @param recommendedSource
     */
    public OntologyCombo(final Map<String, RecommendedOntology> recommendedSource) {

        // set data in combo to be a copy of the data input. This is required due to behaviour of removeAllItems()
        // method in JComboBox which results in removal of elements from the data source also. By making a copy, only
        // the elements in the copy are removed, not the original source.

        // super(data == null ? new String[]{""} : data.clone());

        super(new OntologyTerm[]{});
        super.setEditable(true);

        final SciGraph4Client client = new SciGraph4Client();

        UIHelper.setJComboBoxAsHeavyweight(this);

        // super.setRenderer();

        // add key listener to the combobox editor to listen for input from user.
        super.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                super.keyPressed(e);

                // as long as the key pressed isnt up arrow, down arrow, or return, the proceed
                if ((e.getKeyCode() != 38) && (e.getKeyCode() != 40) &&
                        (e.getKeyCode() != 10)) {
                    String entered = getEditor()
                            .getItem().toString().toLowerCase();

                    final List<String> ontologies = new ArrayList<String>();
                    final String searchTerm;

                    // beta feature
                    if (StringUtils.contains(entered, ":")) {
                        final String[] params = StringUtils.split(entered, ":");
                        ontologies.add(params[0].toUpperCase());
                        searchTerm = params.length > 1 ? params[1] : StringUtils.EMPTY;
                    } else {
                        searchTerm = entered;
                    }

                    if (searchTerm.length() < 3) {
                        return;
                    }

                    removeAllItems();

                    final List<OntologyTerm> terms = new ArrayList<OntologyTerm>();
                    final Map<String, List<OntologyTerm>> results =
                            client.exactSearch(searchTerm, getOntologiesList(ontologies));
                    for (String key : results.keySet()) terms.addAll(results.get(key));

                    OntologyManager.addOLSOntologyDefinitions(
                            client.getOntologyNames(), client.getOntologyVersions());

                    int itemCount = 0;
                    if (terms != null) {
                        for (OntologyTerm aData : terms) {
                            if (aData.getComments().get("Label").toLowerCase().contains(searchTerm)) {
                                addItem(aData);
                                itemCount++;
                            }
                        }
                    }

                    getEditor().setItem(entered);

                    JTextField textField = (JTextField) e.getSource();
                    textField.setCaretPosition(textField.getDocument().getLength());
                    hidePopup();

                    if (itemCount != 0 && isDisplayable()) {
                        showPopup();
                    }
                }
            }

            /**
             * Returns comma separated list of ontologies (or default one)
             *
             * @param ontologies
             * @return
             */
            private String getOntologiesList(final List<String> ontologies) {
                for (String key : recommendedSource.keySet()) {
                    ontologies.add(recommendedSource.get(key).getOntology().getOntologyAbbreviation());
                }
                return recommendedSource.keySet().isEmpty()
                        ? System.getProperty("scigraph.ontology.default", "EFO")
                        : StringUtils.join(ontologies, ",");
            }
        });

        super.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);

                final String entered = getEditor().getItem().toString();

                if (entered.length() < 3) {
                    return;
                }
                removeAllItems();

                final List<String> ontologies = new ArrayList<String>();

                for (String key : recommendedSource.keySet()) {
                    ontologies.add(recommendedSource.get(key).getOntology().getOntologyAbbreviation());
                }

                final String ontology;

                if (recommendedSource.keySet().isEmpty()) {
                    ontology = System.getProperty("scigraph.ontology.default", "EFO");
                } else {
                    ontology = StringUtils.join(ontologies, ",");
                }

                OntologyTerm term = client.getTerm(StringUtils.substringAfter(entered, ":"), ontology);

                addItem(term);

                hidePopup();

                if (isDisplayable()) {
                    showPopup();
                }
            }
        });

    }

    @Override
    public String toString() {
        return getSelectedItem() == null ? "" : getSelectedItem().toString();
    }

}
