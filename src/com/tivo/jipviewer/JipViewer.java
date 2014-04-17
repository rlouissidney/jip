/*/////////////////////////////////////////////////////////////////////

Copyright (C) 2006 TiVo Inc.  All rights reserved.

Redistribution and use in source and binary forms, with or without 
modification, are permitted provided that the following conditions are met:

+ Redistributions of source code must retain the above copyright notice, 
  this list of conditions and the following disclaimer.
+ Redistributions in binary form must reproduce the above copyright notice, 
  this list of conditions and the following disclaimer in the documentation 
  and/or other materials provided with the distribution.
+ Neither the name of TiVo Inc nor the names of its contributors may be 
  used to endorse or promote products derived from this software without 
  specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  POSSIBILITY OF SUCH DAMAGE.

/////////////////////////////////////////////////////////////////////*/

/*/////////////////////////////////////////////////////////////////////
BUGS & RFES

  swing-specific cluelessness on my part
  * where are the headers on self section?  some layout problem.
  * i don't think i should have to add keyListeners to all components
    "manually" to handle the keys globally.  is there something i can
    override?

  * set default sort orders? (either explicitly or in the way i fill
    in the tables)
  * some coloring?
  * some filtering?

  * add a pane for connecting to a remote profiler,
    clearing, starting, stopping, and fetching results.
  ->Done
  
  * make call tree more useful?
    * make stats more regular
    * color?
    * auto-expand?
      * all
      * smart (by cost)
      * and select based on selection in another view...
    * filter (by cost)

  * display class allocation information.
  ->Done

  * why so many interactions?  combine?
  * it'd be cool if we could label "interactions"
  * it'd be cool if we could compress "similar" interactions

Suggestions for jip output:
  * either consistently use the class name table or ditch it
    (i'm inclined to ditch it)
  ->use it
 
Suggestions for jip runtime:
  * make it so we can fetch the xml over the client socket
  ->Done
/////////////////////////////////////////////////////////////////////*/


package com.tivo.jipviewer;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Use JipViewer to open a top-level window viewing the contents of a jip
 * profile.xml file.
 */
public class JipViewer extends JFrame
    implements TreeSelectionListener, ListSelectionListener, KeyListener,
    ChangeListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 5104972020347319592L;
	
	private static int instanceNumero=0;
	
	private static boolean FIRST_HAS_DATA=true;
	
	private JTree  mCallTree;
    private JTable mMethods;
    private ByPackageViewer mPkgViewer;
    private ClassAllocationViewer mClassAllocationViewer;
    private RemoteController mRemoteController;
    
    private TreeNode mCallTreeRoot;
    private ValueModel<JipMethod> mMethodModel = new ValueModel<JipMethod>();
    private MethodRowTableModel mAllMethodsModel = new MethodRowTableModel();
    private TableSorter         mAllMethodsSorterModel;
    
    public static void main(String[] args) throws Exception {
        String filename = "";
        if(args.length == 1) {
            filename = args[0];
        } else {
        	JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("xml data", "xml");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
               filename=chooser.getSelectedFile().getAbsolutePath();
            } 
        }
        JipRun run = new JipRun(filename);
        // System.out.println(run);
       
        long totalTimeForAllThreads = run.getTotalTimeForAllThreads();
        long msec = (long) Math.floor(toMsec(totalTimeForAllThreads));
        String title = "" + msec +" msec -- "+ filename; 
        if ("".equals(filename)) {
           title = "Simple remote control";
           FIRST_HAS_DATA=false;
        }

        new JipViewer(title, run);
    }
    
    public static void createNewViewer(String title, byte[] newXMLData) throws Exception {
    	JipRun run = new JipRun(newXMLData);
    	long totalTimeForAllThreads = run.getTotalTimeForAllThreads();
        long msec = (long) Math.floor(toMsec(totalTimeForAllThreads));
    	new JipViewer("" + msec +" msec -- "+title, run);
    }
    
    public JipViewer(String title, JipRun run) {
        super(title);

        addKeyListener(this);
        mMethodModel.addChangeListener(this);
        
        // build the call tree
        mCallTreeRoot = new TreeNode(title);
        buildTree(run, mCallTreeRoot);

        mCallTree = new JTree(mCallTreeRoot);
        mCallTree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);
        mCallTree.addTreeSelectionListener(this);
        mCallTree.addKeyListener(this);

        // build the allMethods table
        Collection<JipRun.PerMethodInfo> perMethodInfos = run.perMethodsInTotalTimeOrder();
        long totalTimeForAllThreads = run.getTotalTimeForAllThreads();
        for (JipRun.PerMethodInfo perMethod: perMethodInfos) {
            MethodRow row = new MethodRow(perMethod.getMethod());
            for (JipFrame frame: perMethod.allFrames()) {
                if (!frame.isReentrant()) {
                    row.addFrame(frame);
                }
                row.setTimeDenominator(totalTimeForAllThreads);
            }
            mAllMethodsModel.add(row);
        }
        mAllMethodsModel.sort();
        mMethods = MethodViewer.makeTableForMethodRows(mAllMethodsModel);
        mMethods.getSelectionModel().addListSelectionListener(this);
        mMethods.addKeyListener(this);
        mAllMethodsSorterModel = (TableSorter) mMethods.getModel();

        // make the ByPackageViewer
        mPkgViewer = new ByPackageViewer(run);
        mPkgViewer.addKeyListener(this);
        
        // Make the classs count viewer
                
        mClassAllocationViewer = new ClassAllocationViewer(run);
        mClassAllocationViewer.addKeyListener(this);
        
        if (instanceNumero==0) {
          // make the RemoteController only if root
          mRemoteController = new RemoteController();
          mRemoteController.addKeyListener(this);
        }
       
        // make the methodViewer
        MethodViewer methodViewerMethods = new MethodViewer(run, mMethodModel);
        MethodViewer methodViewerCallTree = new MethodViewer(run, mMethodModel);

        // combine all the views
        JTabbedPane tabPane = new JTabbedPane();
        if (instanceNumero!=0 || FIRST_HAS_DATA) {
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                              new JScrollPane(mCallTree),
                                              methodViewerCallTree);
            splitPane.setLayout(new BoxLayout(splitPane, BoxLayout.Y_AXIS));
            tabPane.addTab("call tree", splitPane);      
            splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                   new JScrollPane(mMethods),
                                   methodViewerMethods);
            splitPane.setLayout(new BoxLayout(splitPane, BoxLayout.Y_AXIS));
            tabPane.addTab("methods", splitPane);
            tabPane.addTab("by package", new JScrollPane(mPkgViewer));
            tabPane.addTab("class allocation", mClassAllocationViewer);
        }
        
        if (instanceNumero==0) {
        	tabPane.addTab("remote control", mRemoteController);	
        }
        
        tabPane.addTab("help", new HelpViewer());
        tabPane.addKeyListener(this);
        setContentPane(tabPane);
        pack();
        setSize(new Dimension(1024, 768));
        setVisible(true);
        
        if (instanceNumero==0) {
        	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE ); 
        } else {
        	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE ); 
        }
        instanceNumero++;
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }

        JipMethod method = null;
        ListSelectionModel selectionModel = mMethods.getSelectionModel();
        if (!selectionModel.isSelectionEmpty()) {
            int iSelected = selectionModel.getMinSelectionIndex();
            iSelected = mAllMethodsSorterModel.modelIndex(iSelected);
            MethodRow row = mAllMethodsModel.getRow(iSelected);
            method = row.getMethod();
        }
        //System.out.println("valueChanged("+perMethod+")");
        mMethodModel.setValue(method);
    }

    static class TreeNode extends DefaultMutableTreeNode {
        /**
		 * 
		 */
		private static final long serialVersionUID = 134563573426923193L;

		// we either represent a frame or a random node.
        // if we represent a frame, mFrame is set.
        // otherwise, mLabel is set.
        JipFrame mFrame;

        String mLabel;
        
        
        TreeNode(JipFrame frame) {
            mFrame = frame;
        }

        TreeNode(String label) {
            mLabel = label;
        }

        JipFrame getFrameOrNull() {
            return mFrame;
        }

        JipMethod getMethodOrNull() {
            return (mFrame == null) ? null : mFrame.getMethod();
        }

        public String toString() {
            if (mFrame == null) {
                return mLabel;
            }

            String label = ("(" +
                            toMsec(mFrame.getTotalTime()) + " " +
                            toMsec(mFrame.getNetTime()) + " " +
                            mFrame.getCount() + ") " +
                            mFrame.getMethod().getMethodName());
            return label;
        }

        // returns the first node that has the given method (or null if none)
        private TreeNode findNodeForMethod(TreeNode node,
                                           JipMethod seekingMethod) {
            JipMethod method = node.getMethodOrNull();
            if ((method != null) && method.equals(seekingMethod)) {
                return node;
            }

            int nKid = node.getChildCount();
            for (int iKid=0; iKid < nKid; iKid++) {
                TreeNode kidNode = (TreeNode) node.getChildAt(iKid);
                TreeNode match = findNodeForMethod(kidNode, seekingMethod);
                if (match != null) {
                    return match;
                }
            }

            return null;
        }

        // returns TreePath to the first TreeNode for seekingMethod
        // or null if none.
        TreePath findPathForMethod(JipMethod seekingMethod) {
            TreeNode match = findNodeForMethod(this, seekingMethod);
            if (match == null) {
                return null;
            }

            List<TreeNode> vNode = new ArrayList<TreeNode>();

            TreeNode scan = match;
            while (scan != null) {
                vNode.add(scan);
                scan = (TreeNode) scan.getParent();
            }
            Collections.reverse(vNode);

            return new TreePath(vNode.toArray());
        }
    }

    private void buildTree(JipRun run, DefaultMutableTreeNode root) {
        for(Long threadId: run.sortedThreads()) {
            int i = 1;
            for (JipFrame f: run.interactions(threadId)) {
                String label = ("thread " + threadId + " interaction " + i +
                                " (" + toMsec(f.getTotalTime()) + " msec)");
                TreeNode interactionNode = new TreeNode(label);
                root.add(interactionNode);

                buildFrameTree(interactionNode, f);
                i++;
            }
        }
    }

    private static double toMsec(long time) {
        final double timeToMsec = 1000.0 * 1000.0;
        return Math.floor((time / timeToMsec) * 10) / 10.0;
    }

    private void buildFrameTree(TreeNode parent, JipFrame frame) {
        // compare for reverse total time.
        Comparator<JipFrame> cmp = new Comparator<JipFrame>() {
            public int compare(JipFrame a, JipFrame b) {
                long timeA = a.getTotalTime();
                long timeB = b.getTotalTime();
                if (timeA > timeB) {
                    return -1;
                } else if (timeA < timeB) {
                    return 1;
                } else {
                    String nameA = a.getMethod().getMethodName();
                    String nameB = a.getMethod().getMethodName();
                    return nameB.compareToIgnoreCase(nameA);
                }
            }
        };
        
        TreeNode frameNode = new TreeNode(frame);
        parent.add(frameNode);

        List<JipFrame> vKid = frame.getChildren();
        Collections.sort(vKid, cmp);
        for (JipFrame childFrame: vKid) {
            buildFrameTree(frameNode, childFrame);
        }        
    }
    
    public void valueChanged(TreeSelectionEvent e) {
        TreeNode node = (TreeNode) e.getPath().getLastPathComponent();
        //System.out.println("valueChanged("+node+")");

        JipFrame frame = node.getFrameOrNull();
        JipMethod method = (frame == null) ? null : frame.getMethod();
        mMethodModel.setValue(method);
    }

    //
    // KeyListener
    //

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == 'q') {
            System.exit(0);
        }
    }

    //
    // ChangeListener
    //

    public void changed(Object source) {
        if (source != mMethodModel) {
            throw new RuntimeException("i wish i used asserts!");
        }

        JipMethod selectedMethod = mMethodModel.getValue();
        selectInAllMethods(selectedMethod);
        selectInCallTree(selectedMethod);
    }


    private void selectInAllMethods(JipMethod selectedMethod) {

        if (selectedMethod == null) {
            mMethods.clearSelection();
            return;
        }

        // which row should we select?
        boolean foundIt = false;
        int nRow = mAllMethodsModel.getRowCount();
        int iRow;
        for (iRow = 0; iRow < nRow; iRow++) {
            MethodRow scan = mAllMethodsModel.getRow(iRow);
            if (scan.getMethod().equals(selectedMethod)) {
                foundIt = true;
                break;
            }
        }
        if (!foundIt) {
            System.out.println("couldn't find "+ selectedMethod.getName());
            return;
        }
            
        // update the listSelectionModel
        int iRowInView = mAllMethodsSorterModel.viewIndex(iRow);
        mMethods.getSelectionModel().setSelectionInterval(iRowInView,
                                                          iRowInView);
        
        // scroll to contain the new selection
        Rectangle selectionRect = mMethods.getCellRect(iRowInView,
                                                       0,
                                                       true);
        mMethods.scrollRectToVisible(selectionRect);
    }

    private void selectInCallTree(JipMethod selectedMethod) {

        if (selectedMethod == null) {
            mCallTree.clearSelection();
            return;
        }

        // is this method already selected?
        TreePath curPath = mCallTree.getSelectionPath();
        if (curPath != null) {
            TreeNode  curNode = (TreeNode) curPath.getLastPathComponent();
            JipMethod curMethod = curNode.getMethodOrNull();
            if (curMethod != null &&
                curMethod.equals(selectedMethod)) {
                // we're done.
                return;
            }
        }

        TreePath newPath = mCallTreeRoot.findPathForMethod(selectedMethod);
        if (newPath == null) {
            System.out.println("no path to " + selectedMethod);
            return;
        }


        mCallTree.setSelectionPath(newPath);
        mCallTree.scrollPathToVisible(newPath);
    }
}
