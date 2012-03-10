package org.ballproject.knime.nodegeneration.model.directories;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.ballproject.knime.nodegeneration.model.Directory;
import org.ballproject.knime.nodegeneration.model.directories.source.DescriptorsDirectory;
import org.ballproject.knime.nodegeneration.model.directories.source.ExecutablesDirectory;
import org.ballproject.knime.nodegeneration.model.directories.source.MimeTypesFile;
import org.ballproject.knime.nodegeneration.model.directories.source.PayloadDirectory;
import org.ballproject.knime.nodegeneration.model.mime.MimeType;
import org.dom4j.DocumentException;
import org.jaxen.JaxenException;

public class NodesSourceDirectory extends Directory {

	private Logger logger = Logger.getLogger(NodesSourceDirectory.class
			.getCanonicalName());

	private static final long serialVersionUID = -2772836144406225644L;
	private DescriptorsDirectory descriptorsDirectory = null;
	private ExecutablesDirectory executablesDirectory = null;
	private PayloadDirectory payloadDirectory = null;
	private Properties properties = null;
	private MimeTypesFile mimeTypesFile;

	public NodesSourceDirectory(File nodeSourceDirectory) throws IOException,
			DocumentException {
		super(nodeSourceDirectory);

		try {
			this.descriptorsDirectory = new DescriptorsDirectory(new File(
					nodeSourceDirectory, "descriptors"));
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException("Could not find payload directory "
					+ payloadDirectory.getPath());
		}

		try {
			this.executablesDirectory = new ExecutablesDirectory(new File(
					nodeSourceDirectory, "executables"));
		} catch (FileNotFoundException e) {

		}

		try {
			this.payloadDirectory = new PayloadDirectory(new File(
					nodeSourceDirectory, "payload"));
		} catch (FileNotFoundException e) {

		}

		File propertyFile = new File(nodeSourceDirectory, "plugin.properties");
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(propertyFile));
			this.properties = properties;
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException("Could not find property file "
					+ propertyFile.getPath());
		} catch (IOException e) {
			throw new IOException("Could not load property file", e);
		}

		File mimeTypeFile = new File(descriptorsDirectory, "mimetypes.xml");
		try {
			this.mimeTypesFile = new MimeTypesFile(mimeTypeFile);
		} catch (JaxenException e) {
			throw new IOException("Error reading MIME types from "
					+ mimeTypeFile.getPath());
		}
	}

	public DescriptorsDirectory getDescriptorsDirectory() {
		return descriptorsDirectory;
	}

	public ExecutablesDirectory getExecutablesDirectory() {
		return executablesDirectory;
	}

	public PayloadDirectory getPayloadDirectory() {
		return payloadDirectory;
	}

	public Properties getProperties() {
		return properties;
	}

	public List<MimeType> getMimeTypes() {
		return this.mimeTypesFile.getMimeTypes();
	}
}