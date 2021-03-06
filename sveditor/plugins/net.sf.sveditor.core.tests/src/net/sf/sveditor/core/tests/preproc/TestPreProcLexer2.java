package net.sf.sveditor.core.tests.preproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.sveditor.core.SVCorePlugin;
import net.sf.sveditor.core.StringInputStream;
import net.sf.sveditor.core.db.SVDBFileTree;
import net.sf.sveditor.core.db.SVDBLocation;
import net.sf.sveditor.core.db.index.SVDBFSFileSystemProvider;
import net.sf.sveditor.core.parser.SVLexer;
import net.sf.sveditor.core.parser.SVToken;
import net.sf.sveditor.core.preproc.ISVPreProcFileMapper;
import net.sf.sveditor.core.preproc.ISVPreProcIncFileProvider;
import net.sf.sveditor.core.preproc.SVPathPreProcIncFileProvider;
import net.sf.sveditor.core.preproc.SVPreProcOutput;
import net.sf.sveditor.core.preproc.SVPreProcessor2;
import net.sf.sveditor.core.tests.SVCoreTestCaseBase;
import net.sf.sveditor.core.tests.utils.TestUtils;

public class TestPreProcLexer2 extends SVCoreTestCaseBase {
	
	public void testBasicInclude() {
		SVCorePlugin.getDefault().enableDebug(false);
		File dir1 = new File(fTmpDir, "dir1");
		File dir2 = new File(fTmpDir, "dir2");
		
		assertTrue(dir1.mkdirs());
		assertTrue(dir2.mkdirs());
		
		SVPathPreProcIncFileProvider inc_provider = 
				new SVPathPreProcIncFileProvider(new SVDBFSFileSystemProvider());
			
		inc_provider.addIncdir(dir1.getAbsolutePath());
		inc_provider.addIncdir(dir2.getAbsolutePath());

		TestUtils.copy(
				"class file1;\n" +
				"\n" +
				"endclass\n",
				new File(dir1, "file1.svh"));
		
		TestUtils.copy(
				"/**\n" +
				" * Leading comment\n" +
				" *\n" +
				" */\n" +
				"class file2;\n" +
				"\n" +
				"endclass\n",
				new File(dir1, "file2.svh"));
		
		runTest(
				"\n" +							// 1
				"\n" +							// 2
				"class pre_file1;\n" +			// 3
				"endclass\n" +					// 4
				"`include \"file1.svh\"\n" +	// 5
				"class post_file1;\n" +			// 6
				"endclass\n" +					// 7
				"`include \"file2.svh\"\n" +	// 8
				"class post_file2;\n" +			// 9
				"endclass\n",					// 10
				inc_provider,
				new String[] {
						"class", "pre_file1", ";",
						"endclass",
						
						"class", "file1", ";",
						"endclass",
						
						"class", "post_file1", ";",
						"endclass",
						
						"class", "file2", ";",
						"endclass",
						
						"class", "post_file2", ";",
						"endclass"
				},
				new SVDBLocation[] {
						new SVDBLocation(1, 3, 0), // class
						new SVDBLocation(1, 3, 0), // pre_file1
						new SVDBLocation(1, 3, 0), // ;
						new SVDBLocation(1, 4, 0), // endclass
						new SVDBLocation(2, 1, 0), // class
						new SVDBLocation(2, 1, 0), // file1
						new SVDBLocation(2, 1, 0), // ;
						new SVDBLocation(2, 3, 0), // endclass
						new SVDBLocation(1, 6, 0), // class
						new SVDBLocation(1, 6, 0), // post_file1
						new SVDBLocation(1, 6, 0), // ;
						new SVDBLocation(1, 7, 0), // endclass
						new SVDBLocation(3, 5, 0), // class
						new SVDBLocation(3, 5, 0), // file2
						new SVDBLocation(3, 5, 0), // ;
						new SVDBLocation(3, 7, 0), // endclass
						new SVDBLocation(1, 9, 0), // class
						new SVDBLocation(1, 9, 0), // post_file2
						new SVDBLocation(1, 9, 0), // ;
						new SVDBLocation(1, 10, 0), // endclass
				});
	}

	/*
	public void testBasicDefine() {
		SVCorePlugin.getDefault().enableDebug(false);
		
		SVPathPreProcIncFileProvider inc_provider = 
				new SVPathPreProcIncFileProvider(new SVDBFSFileSystemProvider());
			
		runTest(
				"`define A BB\n" +
				"\n" +
				"`A\n",
				inc_provider,
				"\n" +
				"\n" +
				" BB\n"
				);
	}	

	public void testRecursiveDefine() {
		SVCorePlugin.getDefault().enableDebug(false);
		
		SVPathPreProcIncFileProvider inc_provider = 
				new SVPathPreProcIncFileProvider(new SVDBFSFileSystemProvider());
			
		runTest(
				"`define A(a) \\\n" +
				"	`define MACRO_``a 5\n" +
				"\n" +
				"`A(20)\n" +
				"`MACRO_20\n",
				inc_provider,
				"\n" +
				"\n" +
				" \n" +
				"	\n" +
				" 5\n"
				);
	}
	
	public void testDefineFromInclude() {
		SVCorePlugin.getDefault().enableDebug(false);
		File dir1 = new File(fTmpDir, "dir1");
		File dir2 = new File(fTmpDir, "dir2");
		
		assertTrue(dir1.mkdirs());
		assertTrue(dir2.mkdirs());
		
		SVPathPreProcIncFileProvider inc_provider = 
				new SVPathPreProcIncFileProvider(new SVDBFSFileSystemProvider());
			
		inc_provider.addIncdir(dir1.getAbsolutePath());
		inc_provider.addIncdir(dir2.getAbsolutePath());

		TestUtils.copy(
				"`ifndef A\n" +
				"`define A 5\n" +
				"`endif\n",
				new File(dir1, "file1.svh"));
		
		TestUtils.copy(
				"`ifndef A\n" +
				"`define A 6\n" +
				"`endif\n",
				new File(dir1, "file2.svh"));
		
		runTest(
				"`include \"file1.svh\"\n" +
				"`include \"file2.svh\"\n" +
				"`A\n",
				inc_provider,
				"\n" +
				"\n" +
				"\n" +
				"\n" +
				"  \n" +
				"\n" +
				" 5\n");
	}
	 */
	
	private void runTest(
			String						doc,
			ISVPreProcIncFileProvider	inc_provider,
			String						images[],
			SVDBLocation				locations[]) {
		
		SVPreProcessor2 preproc = new SVPreProcessor2(
				getName(),
				new StringInputStream(doc),
				inc_provider,
				new FileMapper());
	
		SVPreProcOutput output = preproc.preprocess();
		
		List<SVPreProcOutput.FileChangeInfo> file_map = output.getFileMap();
		
		for (SVPreProcOutput.FileChangeInfo e : file_map) {
			fLog.debug("FileMap Entry: " + e.fStartIdx + " " + e.fFileId + " " + e.fLineno);
		}
		
		SVLexer lexer = new SVLexer();
		lexer.init(null, output);
		
		fLog.debug("Output:\n" + output.dump());

		SVToken t;
		int idx = 0;
		while ((t = lexer.consumeToken()) != null) {
			fLog.debug("Token: " + t.getImage() + " @ " + 
					t.getStartLocation().getFileId() + ":" +
					t.getStartLocation().getLine());
			assertEquals(images[idx], t.getImage());
			assertEquals("File ID of " + images[idx] + "(" + idx + ")", 
					locations[idx].getFileId(), t.getStartLocation().getFileId());
			assertEquals("Line of " + images[idx] + "(" + idx + ")", 
					locations[idx].getLine(), t.getStartLocation().getLine());
			idx++;
		}
	}
	
	private class FileMapper implements ISVPreProcFileMapper {
		private List<String>				fFileMap;
		
		public FileMapper() {
			fFileMap = new ArrayList<String>();
		}

		@Override
		public int mapFilePathToId(String path, boolean add) {
			int idx = fFileMap.indexOf(path);
			
			if (idx == -1) {
				if (add) {
					fFileMap.add(path);
					return fFileMap.size();
				} else {
					return -1;
				}
			} else {
				return (idx+1);
			}
		}

		@Override
		public String mapFileIdToPath(int id) {
			return fFileMap.get(id-1);
		}
		
	}
	
	private void printFileTree(String ind, SVDBFileTree ft) {
		fLog.debug(ind + "FileTree: " + ft.fFilePath);
		
		for (SVDBFileTree ft_s : ft.fIncludedFileTrees) {
			printFileTree(ind + "  ", ft_s);
		}
	}

}
