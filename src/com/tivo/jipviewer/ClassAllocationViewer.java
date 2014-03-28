/*
 Copyright (c) 2014, Rodolphe LOUIS-SIDNEY
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 + Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.

 + Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

 + Neither the name of the project's author nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.tivo.jipviewer;

import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ClassAllocationViewer extends Container implements ChangeListener, MouseListener  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2070151578674017990L;
	
	private JTable mTable;
	private JipRun mRun;
	static int svWidth[] = {200, 780};
	
	public ClassAllocationViewer(JipRun run) {
		   mRun = run;
		   ClassAllocationTableModel dataModel = new ClassAllocationTableModel();
		   Iterable<String> itClassCount = mRun.getmClassCount().keySet();
		   Map<String, String> pn = mRun.getmFullClassNames();
		   for (String key : itClassCount) {
		    	String pnName = pn.get(key);
		    	if (pnName!=null) {
		          dataModel.add(new ClassAllocationRow(pnName.replace("/", "."),
		        		        mRun.getmClassCount().get(key)));
		    	} else {
		    	  dataModel.add(new ClassAllocationRow(key.replace("/", "."),
		    			        mRun.getmClassCount().get(key)));	
		    	}
		   }     
		   dataModel.sort();
		   TableSorter sorter = new TableSorter(dataModel);
		   mTable = new JTable(sorter);
		   sorter.setTableHeader(mTable.getTableHeader());

		   TableColumnModel colModel = mTable.getColumnModel();
		   int nCol = dataModel.getColumnCount();
		   for (int iCol=0; iCol < nCol; iCol++) {
		       TableColumn col = colModel.getColumn(iCol);
		       if (iCol < svWidth.length) {
		          col.setPreferredWidth(svWidth[iCol]);
		       }
		   }
		   mTable.doLayout();
		   mTable.addMouseListener(this);
		   add(new JScrollPane(mTable));
		   setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void changed(Object source) {
		
	}
}
