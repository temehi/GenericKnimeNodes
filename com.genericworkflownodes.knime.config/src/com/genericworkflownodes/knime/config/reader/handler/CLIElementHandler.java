/**
 * Copyright (c) 2012, Stephan Aiche.
 *
 * This file is part of GenericKnimeNodes.
 * 
 * GenericKnimeNodes is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.genericworkflownodes.knime.config.reader.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.genericworkflownodes.knime.cliwrapper.CLI;
import com.genericworkflownodes.knime.cliwrapper.CLIElement;
import com.genericworkflownodes.knime.cliwrapper.CLIMapping;
import com.genericworkflownodes.knime.config.NodeConfiguration;

/**
 * The ContentHandler for the CLI element.
 * 
 * @author aiche
 */
public class CLIElementHandler extends DefaultHandler {

    private static final String TAG_CLI = "cli";
    private static final String TAG_CLIELEMENT = "clielement";
    private static final String TAG_MAPPING = "mapping";

    private static final String ATTR_OPTION_IDENTIFIER = "optionIdentifier";
    private static final String ATTR_ISLIST = "isList";
    private static final String ATTR_REQUIRED = "isRequired";
    private static final String ATTR_REFNAME = "referenceName";

    /**
     * The CLI that should be generated by this element handler.
     */
    private CLI m_readCLI;

    /**
     * Current element.
     */
    private CLIElement m_currentElement;

    /**
     * The {@link NodeConfiguration} that will be filled while parsing the
     * document.
     */
    private NodeConfiguration m_config;

    /**
     * The parent handler that invoked this handler for a sub tree of the XML
     * document.
     */
    private CTDHandler m_parentHandler;

    /**
     * The {@link XMLReader} that processes the entire document.
     */
    private XMLReader m_xmlReader;

    /**
     * C'tor.
     * 
     * @param xmlReader
     *            The {@link XMLReader} used for parsing the complete document.
     * @param parentHandler
     *            The parent handler that triggered this handler.
     * @param config
     *            The {@link NodeConfiguration} that will be filled while
     *            parsing the document.
     */
    public CLIElementHandler(XMLReader xmlReader, CTDHandler parentHandler,
            NodeConfiguration config) {
        m_xmlReader = xmlReader;
        m_parentHandler = parentHandler;
        m_readCLI = new CLI();
        m_config = config;
    }

    @Override
    public void startElement(String uri, String localName, String name,
            Attributes attributes) throws SAXException {
        if (TAG_CLIELEMENT.equals(name)) {
            // we start a new element
            m_currentElement = new CLIElement();
            String isList = attributes.getValue(ATTR_ISLIST);
            m_currentElement
                    .setIsList((isList != null && "true".equals(isList)));
            String isRequired = attributes.getValue(ATTR_REQUIRED);
            m_currentElement.setRequired((isRequired != null && "true"
                    .equals(isRequired)));
            m_currentElement.setOptionIdentifier(attributes
                    .getValue(ATTR_OPTION_IDENTIFIER));
        } else if (TAG_MAPPING.equals(name)) {
            CLIMapping mapping = new CLIMapping();
            String refName = attributes.getValue(ATTR_REFNAME);
            mapping.setReferenceName(refName);
            m_currentElement.getMapping().add(mapping);
        }
    }

    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        if (TAG_CLI.equals(name)) {
            // return to parent scope
            m_config.setCLI(m_readCLI);
            m_xmlReader.setContentHandler(m_parentHandler);
        } else if (TAG_CLIELEMENT.equals(name)) {
            // finished reading this element
            m_readCLI.getCLIElement().add(m_currentElement);
        } else {
            super.endElement(uri, localName, name);
        }
    }
}