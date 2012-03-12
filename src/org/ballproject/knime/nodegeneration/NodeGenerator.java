/*
 * Copyright (c) 2011-2012, Marc Röttig.
 * Copyright (c) 2012, Björn Kahlert.
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

package org.ballproject.knime.nodegeneration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.ballproject.knime.base.config.CTDNodeConfigurationReaderException;
import org.ballproject.knime.base.config.INodeConfiguration;
import org.ballproject.knime.base.util.Helper;
import org.ballproject.knime.nodegeneration.exceptions.DuplicateNodeNameException;
import org.ballproject.knime.nodegeneration.exceptions.InvalidNodeNameException;
import org.ballproject.knime.nodegeneration.exceptions.UnknownMimeTypeException;
import org.ballproject.knime.nodegeneration.model.KNIMEPluginMeta;
import org.ballproject.knime.nodegeneration.model.directories.NodesBuildDirectory;
import org.ballproject.knime.nodegeneration.model.directories.NodesSourceDirectory;
import org.ballproject.knime.nodegeneration.model.directories.build.NodesBuildKnimeNodesDirectory;
import org.ballproject.knime.nodegeneration.model.directories.source.DescriptorsDirectory;
import org.ballproject.knime.nodegeneration.model.files.CTDFile;
import org.ballproject.knime.nodegeneration.templates.BinaryResourcesTemplate;
import org.ballproject.knime.nodegeneration.templates.ManifestMFTemplate;
import org.ballproject.knime.nodegeneration.templates.MimeFileCellFactoryTemplate;
import org.ballproject.knime.nodegeneration.templates.NodeDialogTemplate;
import org.ballproject.knime.nodegeneration.templates.NodeFactoryTemplate;
import org.ballproject.knime.nodegeneration.templates.NodeFactoryXMLTemplate;
import org.ballproject.knime.nodegeneration.templates.NodeModelTemplate;
import org.ballproject.knime.nodegeneration.templates.NodeViewTemplate;
import org.ballproject.knime.nodegeneration.templates.PluginActivatorTemplate;
import org.ballproject.knime.nodegeneration.templates.PluginXMLTemplate;
import org.ballproject.knime.nodegeneration.util.NodeDescriptionUtils;
import org.ballproject.knime.nodegeneration.util.Utils;
import org.ballproject.knime.nodegeneration.writer.DatWriter;
import org.ballproject.knime.nodegeneration.writer.PropertiesWriter;
import org.dom4j.DocumentException;
import org.eclipse.core.commands.ExecutionException;
import org.knime.core.node.NodeFactory;

public class NodeGenerator {
	private static final Logger LOGGER = Logger.getLogger(NodeGenerator.class
			.getCanonicalName());

	private NodesSourceDirectory srcDir;
	private KNIMEPluginMeta meta;
	private NodesBuildDirectory buildDir;

	@SuppressWarnings("serial")
	public NodeGenerator(File pluginDir) throws IOException,
			ExecutionException, DocumentException, DuplicateNodeNameException,
			InvalidNodeNameException, CTDNodeConfigurationReaderException,
			UnknownMimeTypeException {

		this.srcDir = new NodesSourceDirectory(pluginDir);
		this.meta = new KNIMEPluginMeta(srcDir.getProperties());
		this.buildDir = new NodesBuildDirectory(meta.getPackageRoot());

		LOGGER.info("Creating KNIME plugin sources\n\tFrom" + this.srcDir
				+ "\n\tTo: " + this.buildDir);

		boolean dynamicCTDs = NodeDescriptionUtils
				.createCTDsIfNecessary(srcDir);
		DescriptorsDirectory descriptorsDirectory = (dynamicCTDs) ? new DescriptorsDirectory(
				srcDir.getExecutablesDirectory()) : srcDir
				.getDescriptorsDirectory();

		if (dynamicCTDs)
			LOGGER.info("Using dynamically created ctd files");
		else
			LOGGER.info("Using static ctd files");

		// META-INF/MANIFEST.MF
		new ManifestMFTemplate(meta).write(buildDir.getManifestMf());

		// src/[PACKAGE]/knime/PluginActivator.java
		new PluginActivatorTemplate(meta.getPackageRoot()).write(new File(
				this.buildDir.getKnimeDirectory(), "PluginActivator.java"));

		// src/[PACKAGE]/knime/plugin.properties
		new PropertiesWriter(new File(this.buildDir.getKnimeDirectory(),
				"plugin.properties")).write(new HashMap<String, String>() {
			{
				put("use_ini", srcDir.getProperty("use_ini", "true"));
				put("ini_switch", srcDir.getProperty("ini_switch", "-ini"));
			}
		});

		// src/[PACKAGE]/knime/InternalTools.dat
		new DatWriter(new File(this.buildDir.getKnimeDirectory(),
				"InternalTools.dat")).write(descriptorsDirectory
				.getInternalCtdFiles());

		// src/[PACKAGE]/knime/ExternalTools.dat
		new DatWriter(new File(this.buildDir.getKnimeDirectory(),
				"ExternalTools.dat")).write(descriptorsDirectory
				.getExternalCtdFiles());

		// src/[PACKAGE]/knime/nodes/mimetypes/MimeFileCellFactory.java
		new MimeFileCellFactoryTemplate(meta.getPackageRoot(),
				srcDir.getMimeTypes()).write(new File(buildDir
				.getKnimeNodesDirectory(), "mimetypes" + File.separator
				+ "MimeFileCellFactory.java"));

		PluginXMLTemplate pluginXML = new PluginXMLTemplate();

		// src/[PACKAGE]/knime/nodes/*/*
		for (CTDFile ctdFile : descriptorsDirectory.getCTDFiles()) {
			LOGGER.info("Start processing ctd file: " + ctdFile.getName());
			String factoryClass = copyNodeSources(ctdFile,
					this.buildDir.getKnimeNodesDirectory(), meta);

			String absoluteCategory = "/" + meta.getNodeRepositoryRoot() + "/"
					+ meta.getName() + "/"
					+ ctdFile.getNodeConfiguration().getCategory();
			pluginXML.registerNode(factoryClass, absoluteCategory);

			// TODO
			// this.installIcon();
		}

		// plugin.xml
		pluginXML.saveTo(buildDir.getPluginXml());

		// src/[PACKAGE]/knime/nodes/binres/BinaryResources.java
		new BinaryResourcesTemplate(meta.getPackageRoot()).write(new File(
				this.buildDir.getBinaryResourcesDirectory(),
				"BinaryResources.java"));

		// src/[PACKAGE]/knime/nodes/binres/*.ini *.zip
		this.srcDir.getPayloadDirectory().copyPayloadTo(
				this.buildDir.getBinaryResourcesDirectory());
	}

	public File getSourceDirectory() {
		return this.srcDir;
	}

	public File getBuildDirectory() {
		return this.buildDir;
	}

	public String getPluginName() {
		return meta.getName();
	}

	public String getPluginVersion() {
		return meta.getVersion();
	}

	// TODO
	// public void installIcon() throws IOException {
	// if (this._iconpath_ != null) {
	// Node node = this.plugindoc
	// .selectSingleNode("/plugin/extension[@point='org.knime.product.splashExtension']");
	// Element elem = (Element) node;
	//
	// elem.addElement("splashExtension")
	// .addAttribute("icon", "icons/logo.png")
	// .addAttribute("id", "logo");
	//
	// new File(this._destdir_ + File.separator + "icons").mkdirs();
	// Helper.copyFile(new File(this._iconpath_), new File(this._destdir_
	// + File.separator + "icons" + File.separator + "logo.png"));
	// }
	//
	// }

	/**
	 * Copies the java sources needed to invoke a tool (described by a
	 * {@link CTDFile}) to the specified {@link NodesBuildKnimeNodesDirectory}.
	 * 
	 * @param ctdFile
	 *            which described the wrapped tool
	 * @param nodesDir
	 *            location where to create a sub directory containing the
	 *            generated sources
	 * @param pluginMeta
	 *            meta information used to adapt the java files
	 * @return the fully qualified name of the {@link NodeFactory} class able to
	 *         build instances of the node.
	 * @throws IOException
	 * @throws UnknownMimeTypeException
	 */
	public static String copyNodeSources(CTDFile ctdFile,
			NodesBuildKnimeNodesDirectory nodesDir, KNIMEPluginMeta pluginMeta)
			throws IOException, UnknownMimeTypeException {

		INodeConfiguration nodeConfiguration = ctdFile.getNodeConfiguration();
		String nodeName = Utils.fixKNIMENodeName(nodeConfiguration.getName());

		File nodeSourceDir = new File(nodesDir, nodeName);
		nodeSourceDir.mkdirs();

		// src/[PACKAGE]/knime/nodes/[NODE_NAME]/NodeDialog.java
		new NodeDialogTemplate(pluginMeta.getPackageRoot(), nodeName)
				.write(new File(nodeSourceDir, nodeName + "NodeDialog.java"));

		// src/[PACKAGE]/knime/nodes/[NODE_NAME]/NodeDialog.java
		new NodeViewTemplate(pluginMeta.getPackageRoot(), nodeName)
				.write(new File(nodeSourceDir, nodeName + "NodeView.java"));

		// src/[PACKAGE]/knime/nodes/[NODE_NAME]/NodeModel.java
		new NodeModelTemplate(pluginMeta.getPackageRoot(), nodeName,
				nodeConfiguration).write(new File(nodeSourceDir, nodeName
				+ "NodeModel.java"));

		// src/[PACKAGE]/knime/nodes/[NODE_NAME]/NodeFactory.xml
		new NodeFactoryXMLTemplate(nodeName, nodeConfiguration).write(new File(
				nodeSourceDir, nodeName + "NodeFactory.xml"));

		// src/[PACKAGE]/knime/nodes/[NODE_NAME]/NodeFactory.java
		new NodeFactoryTemplate(pluginMeta.getPackageRoot(), nodeName)
				.write(new File(nodeSourceDir, nodeName + "NodeFactory.java"));

		File nodeConfigDir = new File(nodeSourceDir, "config");
		nodeConfigDir.mkdirs();

		// src/[PACKAGE]/knime/nodes/[NODE_NAME]/config/config.xml
		Helper.copyFile(ctdFile, new File(nodeConfigDir, "config.xml"));

		return pluginMeta.getPackageRoot() + ".knime.nodes." + nodeName + "."
				+ nodeName + "NodeFactory";
	}

}
