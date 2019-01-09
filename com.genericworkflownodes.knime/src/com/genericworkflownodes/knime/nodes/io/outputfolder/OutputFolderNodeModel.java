package com.genericworkflownodes.knime.nodes.io.outputfolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.uri.IURIPortObject;
import org.knime.core.data.uri.URIContent;
import org.knime.core.data.uri.URIPortObjectSpec;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.util.FileUtil;

/**
 * This is the model implementation of OutputFolder. Writes all the incoming
 * files to the given output folder.
 * 
 * @author The GKN Team
 */
public class OutputFolderNodeModel extends NodeModel {

    private static final String DEFAULT_FOLDER_NAME_VALUE = "";

    static final String CFG_FOLDER_NAME = "FOLDERNAME";

    static final String CFG_CREATE_FOLDER = "CREATE_IF_NOT_EXISTS";
    
    SettingsModelString m_foldername = new SettingsModelString(
            OutputFolderNodeModel.CFG_FOLDER_NAME, DEFAULT_FOLDER_NAME_VALUE);

    SettingsModelBoolean m_createIfNotExists = new SettingsModelBoolean(CFG_CREATE_FOLDER, false);
    
    /**
     * Constructor for the node model.
     */
    protected OutputFolderNodeModel() {
        super(new PortType[] { PortTypeRegistry.getInstance().getPortType(IURIPortObject.class) },
                new PortType[] {});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects,
            final ExecutionContext exec) throws Exception {

        IURIPortObject obj = (IURIPortObject) inObjects[0];
        List<URIContent> uris = obj.getURIContents();

        if (uris.size() == 0) {
            throw new Exception(
                    "There were no URIs in the supplied URIPortObject");
        }
        File folder = FileUtil.getFileFromURL(FileUtil.toURL(m_foldername.getStringValue()));
        
        if (!folder.exists()) {
            if (m_createIfNotExists.getBooleanValue()) {
                folder.mkdirs();
            } else {
                throw new InvalidSettingsException("The selected folder does not exist. "
                        + "Check \"Create folder if it does not exist\" in the configuration "
                        + "or select a different one.");
            }
        }
        
        double idx = 1.0;
        for (URIContent uri : uris) {
            File in = FileUtil.getFileFromURL(uri.getURI().toURL());
            if (!in.canRead()) {
                throw new Exception("Cannot read file to export: "
                        + in.getAbsolutePath());
            }

            File target = new File(folder, in.getName());

            if (target.exists() && !target.canWrite()) {
                throw new Exception("Cannot write to file: "
                        + target.getAbsolutePath());
            } else if (!target.getParentFile().canWrite()) {
                throw new Exception("Cannot write to containing directoy: "
                        + target.getParentFile().getAbsolutePath());
            }

            FileUtils.copyFile(in, target);
            exec.setProgress(idx / uris.size());
            exec.checkCanceled();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        if (!(inSpecs[0] instanceof URIPortObjectSpec)) {
            throw new InvalidSettingsException(
                    "No URIPortObjectSpec compatible port object");
        }

        // check the selected file
        if ("".equals(m_foldername.getStringValue())) {
            throw new InvalidSettingsException(
                    "Please select a target folder for the Output Folder node.");
        }
        return new DataTableSpec[] {};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_foldername.saveSettingsTo(settings);
        m_createIfNotExists.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_foldername.loadSettingsFrom(settings);
        if (settings.containsKey(CFG_CREATE_FOLDER)) {
            m_createIfNotExists.loadSettingsFrom(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_foldername.validateSettings(settings);
        if (settings.containsKey(CFG_CREATE_FOLDER)) {
            m_createIfNotExists.validateSettings(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

}
