package net.sf.sveditor.core.script.launch;

import java.util.ArrayList;
import java.util.List;

import net.sf.sveditor.core.ILineListener;

public class LogMessageScannerMgr implements ILogMessageScannerMgr, ILineListener {
	private String							fWorkingDir;
	private List<ILogMessageScanner>		fScanners;
	private List<ScriptMessage>				fMessages;
	
	public LogMessageScannerMgr(String working_dir) {
		fWorkingDir = working_dir;
		fScanners = new ArrayList<ILogMessageScanner>();
		fMessages = new ArrayList<ScriptMessage>();
	}
	
	public void addScanner(ILogMessageScanner scanner) {
		scanner.init(this);
		fScanners.add(scanner);
	}

	public List<ScriptMessage> getMessages() {
		return fMessages;
	}
	
	public void setWorkingDirectory(String path) {
		fWorkingDir = path;
	}

	@Override
	public void setWorkingDirectory(String path, ILogMessageScanner scanner) {
		fWorkingDir = path;
	}

	@Override
	public String getWorkingDirectory() {
		return fWorkingDir;
	}

	@Override
	public void addMessage(ScriptMessage msg) {
		fMessages.add(msg);
	}

	@Override
	public void line(String l) {
		synchronized (fScanners) {
			// First, provide the line to any scanners that might change
			// the working directory
			for (ILogMessageScanner s : fScanners) {
				if (s.providesDirectory()) {
					s.line(l);
				}
			}
			
			// Then, provide the line to scanners that won't change the
			// working directory
			for (ILogMessageScanner s : fScanners) {
				if (!s.providesDirectory()) {
					s.line(l);
				}
			}
		}
	}
}
