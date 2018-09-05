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
package com.genericworkflownodes.knime.execution;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.genericworkflownodes.knime.nodes.exttool.ExtToolOutputNodeModel;

import com.genericworkflownodes.knime.config.INodeConfiguration;
import com.genericworkflownodes.knime.custom.config.IPluginConfiguration;

/**
 * Defines a tool executor for generic tools.
 * 
 * @author aiche
 */
public interface IToolExecutor {

    /**
     * Defines which command generator should be used to create the command line
     * call for the execution.
     * 
     * @param generator
     *            The {@link ICommandGenerator} that should be used to generate
     *            the execute call.
     */
    void setCommandGenerator(ICommandGenerator generator);

    /**
     * Retrieves the command generator used by this executor.
     * 
     * @return The command generator.
     */
    ICommandGenerator getCommandGenerator();

    /**
     * The execute method used by derived classes to execute their command.
     * 
     * @return The return value of the executed process.
     * @throws Exception
     *             In case of errors.
     */
    int execute() throws ToolExecutionFailedException;

    /**
     * Initialization method of the executor.
     * 
     * @param nodeConfiguration
     * @param pluginConfiguration
     * 
     * @throws Exception
     *             In case of errors.
     */
    void prepareExecution(INodeConfiguration nodeConfiguration,
            IPluginConfiguration pluginConfiguration) throws Exception;
    
    /**
     * Initialization method of the executor.
     * 
     * @param model The NodeModel e.g. used to update Views
     */
    void setModel(ExtToolOutputNodeModel model);

    /**
     * Kills the running process.
     */
    void kill();

    /**
     * Returns the return value of the process. If the tool didn't not run or is
     * not finished it is set to -1.
     * 
     * @return The return code of the executed process.
     */
    int getReturnCode();

    /**
     * Sets the working directory of the process. If the directory does not
     * exist or the @p path does not point to a directory (but a file), an
     * exception will be thrown.
     * 
     * @param directory
     *            The new working directory.
     * @throws IOException
     *             If the path does not exist or points to a file (and not a
     *             directory).
     */
    void setWorkingDirectory(File directory) throws IOException;
    
    /**
     * Returns the command used to execute the tool as a list of strings.
     * 
     * @return The command arguments to execute the tool.
     */
    List<String> getCommand();

    /**
     * Returns the standard output generated by the tool as list of strings.
     * 
     * @return The output of the tool.
     */
    LinkedList<String> getToolOutput();

    /**
     * Returns the error output (i.e., stderr) generated by the tool as a
     * list of strings.
     * 
     * @return The error output of the tool.
     */
    LinkedList<String> getToolErrorOutput();

}
