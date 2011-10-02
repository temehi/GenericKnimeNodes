/*
 * Copyright (c) 2011, Marc Röttig.
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

package org.ballproject.knime.base.io.demangler;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.ballproject.knime.GenericNodesPlugin;
import org.ballproject.knime.base.mime.MIMEFileCell;
import org.ballproject.knime.base.mime.MIMEtypeRegistry;
import org.ballproject.knime.base.mime.demangler.Demangler;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;



/**
 * This is the model implementation of DemanglerNodeModel.
 * 
 * 
 * @author roettig
 */
public class DemanglerNodeModel extends NodeModel
{

	// the logger instance
	private static final NodeLogger logger = NodeLogger.getLogger(DemanglerNodeModel.class);
	
	protected List<Demangler>  demanglers = new ArrayList<Demangler>();
	protected MIMEtypeRegistry resolver = GenericNodesPlugin.getMIMEtypeRegistry();
	protected Demangler  demangler;
	
	/**
	 * Constructor for the node model.
	 */
	protected DemanglerNodeModel()
	{
		super(1, 1);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception
	{
		BufferedDataContainer container = null;
		DataCell inCell0 = inData[0].iterator().next().getCell(0);
		
		if(inCell0.getType().isCollectionType())
		{
			ListCell lc = (ListCell) inCell0;
			int N = lc.size();
			container = exec.createDataContainer(adjustOutSpec(N));
			List<Iterator<DataCell>> iters = new ArrayList<Iterator<DataCell>>();
			for(DataCell dc: lc)
			{
				if(! (dc instanceof MIMEFileCell) )
				{
					throw new Exception("ListCell does not contain MIMEFileCells");
				}
				MIMEFileCell mfc = (MIMEFileCell) dc;
				iters.add( demangler.demangle(mfc) );
			}
			fillTable( iters, container);
		}
		else
		{
			container = exec.createDataContainer(outspec);
			if(! (inCell0 instanceof MIMEFileCell) )
			{
				throw new Exception("first DataCell is not a MIMEFileCell");
			}
			List<Iterator<DataCell>> iters = new ArrayList<Iterator<DataCell>>();
			MIMEFileCell mfc = (MIMEFileCell) inCell0;
			iters.add( demangler.demangle(mfc) );
			fillTable( iters, container);
		}
		container.close();
		
		BufferedDataTable out = container.getTable();
		
		return new BufferedDataTable[]{ out };
	}

	private void fillTable(List<Iterator<DataCell>> iters, BufferedDataContainer container)
	{
		int C   = iters.size();
		int idx = 1;
		while(true)
		{
			DataCell[] rowcells = new DataCell[C];
			int nDepleted = 0;
			for(int i=0;i<C;i++)
			{
				if(iters.get(i).hasNext())
				{
					rowcells[i] = iters.get(i).next();
				}
				else
				{
					nDepleted++;
					rowcells[i] = DataType.getMissingCell();
				}
			}
			
			// all iterators are depleted
			if(nDepleted==C)
			{
				break;
			}
			
			DataRow row = new DefaultRow("Row "+idx, rowcells);
			
			container.addRowToTable(row);	
			idx++;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset()
	{
		// TODO Code executed on reset.
		// Models build during execute are cleared here.
		// Also data handled in load/saveInternals will be erased here.
	}
	
	
	protected DataType inType;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException
	{	
		// get data type of first column (where the MIMEFileCell is stored by convention)
		inType = inSpecs[0].getColumnSpec(0).getType();
		
		boolean coll = false;
		
		if(inType.isCollectionType())
		{
			coll = true;
			inType = inType.getCollectionElementType();
		}
		
		// try to find a demangler for the data type ... 
		demanglers = resolver.getDemangler(inType);
		
		if(demanglers==null)
		{
			throw new InvalidSettingsException("no Demangler found for "+inType.toString()+". Please register one first.");
		}
		
		demangler  = demanglers.get(idx);
		
		if(coll)
		{
			return new DataTableSpec[]{null};
		}
		
		return new DataTableSpec[]{ getDataTableSpec() };
	}

	private DataTableSpec outspec;
	
	private DataTableSpec getDataTableSpec() throws InvalidSettingsException
	{
		DataColumnSpec[] allColSpecs = new DataColumnSpec[1];
        
        DataType      dt =  demangler.getTargetType();
		allColSpecs[0]   =  new DataColumnSpecCreator("column 0",  dt).createSpec();
        DataTableSpec outputSpec = new DataTableSpec(allColSpecs);

        // save this internally
        outspec = outputSpec;
        
        return outputSpec;
	}
	
	private DataTableSpec adjustOutSpec(int N) throws InvalidSettingsException
	{
		DataColumnSpec[] allColSpecs = new DataColumnSpec[N];
        for(int i=0;i<N;i++)
        {
        	allColSpecs[i] = new DataColumnSpecCreator("column "+i, demangler.getTargetType()).createSpec();	
        }
        
        DataTableSpec outputSpec = new DataTableSpec(allColSpecs);

        // save this internally
        outspec = outputSpec;
        
        return outputSpec;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings)
	{
		String[] names = new String[demanglers.size()];
		int i = 0;
		for(Demangler demangler: demanglers)
		{
			names[i++] = demangler.getClass().getCanonicalName();

		}
		settings.addStringArray("demanglers", names);
		settings.addInt("selected_index", idx);
	}

	private int idx = 0;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException
	{
		idx = settings.getInt("selected_index");
		if(demanglers.size()!=0)
			demangler = demanglers.get(idx);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec) throws IOException, CanceledExecutionException
	{		
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(internDir,"demanglers")));
    	try
		{
			demanglers = (List<Demangler>) in.readObject();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		in.close();
		
		// create the file 
		File f = new File(internDir, "selected_index");
		// load the settings from the file 
		NodeSettingsRO settings = NodeSettings.loadFromXML(new FileInputStream(f));
		// retrieve the stored values
		try
		{
			idx = settings.getInt("selected_index");
		} 
		catch (InvalidSettingsException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec) throws IOException, CanceledExecutionException
	{
		DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(internDir,"demanglers")));

	    ObjectOutput          oout = new ObjectOutputStream(out) ;
	    oout.writeObject(this.demanglers);
	    oout.close();

	    // create a settings object with a config name
	    NodeSettings settings = new NodeSettings("selected_index");
	    // store your values under a certain key
	    settings.addInt("selected_index", idx);
	    // create a file in the given directory
	    File f = new File(internDir, "selected_index");
	    // and save it
	    settings.saveToXML(new FileOutputStream(f));
	}
	
}
