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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class ClassAllocationTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1283383192784034965L;
	
	public static final int COUNT         = 0;
    public static final int CLASS_NAME    = 1;


    List<ClassAllocationRow> caRow = new ArrayList<ClassAllocationRow>();

    public void add(ClassAllocationRow row) {
        caRow.add(row);
        int iLast = caRow.size();
        fireTableRowsInserted(iLast, iLast);
    }

    public void clear() {
        int iLast = caRow.size();
        caRow.clear();
        fireTableRowsDeleted(0, iLast);
    }

    public ClassAllocationRow getRow(int iRow) {
        return caRow.get(iRow);
    }

    public int getRowCount() {
        return caRow.size();
    }
    
    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int iColumn) {
        switch (iColumn) {
          case COUNT:         return "count";
          case CLASS_NAME:    return "class";
          default:            throw new RuntimeException("bad column index");
        }
    }
        
    public Class<?> getColumnClass(int iColumn) {
        switch (iColumn) {
          case COUNT:         return Long.class;
          case CLASS_NAME:    return String.class;
          default:            throw new RuntimeException("bad column index");
        }
    }

    public Object getValueAt(int iRow, int iColumn) {
        ClassAllocationRow row =  caRow.get(iRow);
        switch (iColumn) {
          case COUNT:         return row.getCount();
          case CLASS_NAME:    return row.getClassName();
          default:            throw new RuntimeException("bad column index");
        }
    }

	public void sort() {
		Collections.sort(caRow, new Comparator<ClassAllocationRow>() {
		    public int compare(ClassAllocationRow o1, ClassAllocationRow o2) {
		        return o2.getCount().compareTo(o1.getCount());
		    }
		});
	}
}
