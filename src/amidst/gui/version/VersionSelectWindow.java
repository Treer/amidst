package amidst.gui.version;

import java.awt.Container;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import amidst.Amidst;
import amidst.Options;
import amidst.Util;
import amidst.logging.Log;
import amidst.version.LatestVersionList;
import amidst.version.MinecraftProfile;
import amidst.version.MinecraftVersion;
import amidst.version.VersionFactory;

public class VersionSelectWindow extends JFrame {
	private static VersionSelectWindow instance;
	private VersionFactory versionFactory = new VersionFactory();
	
	public VersionSelectWindow() {
		super("Profile Selector");
		instance = this;
		setIconImages(Amidst.icons);
		Container contentPane = getContentPane();
		contentPane.setLayout(new MigLayout());
		
		LatestVersionList.get().load(true);
		
		if (!Util.minecraftDirectory.exists() || !Util.minecraftDirectory.isDirectory()) {
			Log.crash("Unable to find Minecraft directory at: " + Util.minecraftDirectory);
			return;
		}
		
		final JLabel titleLabel = new JLabel("Please select a Minecraft version:", SwingConstants.CENTER);
		titleLabel.setFont(new Font("arial", Font.BOLD, 16));
		
		add(titleLabel, "h 20!,w :400:, growx, pushx, wrap");

		final VersionSelectPanel versionSelector = new VersionSelectPanel();
		
		(new Thread(new Runnable() {
			@Override
			public void run() {
				versionFactory.scanForProfiles();
				MinecraftProfile[] profileVersions = versionFactory.getProfiles();
				versionFactory.scanForLocalVersions();
				MinecraftVersion[] localVersions = versionFactory.getLocalVersions();
				String selectedProfile = Options.instance.lastProfile.get();
				
				if ((profileVersions == null || profileVersions.length == 0) && (localVersions == null || localVersions.length == 0)) {
					versionSelector.setEmptyMessage("Empty");
					return;
				}
				for (int i = 0; i < profileVersions.length; i++) {
					versionSelector.addVersion(new ProfileVersionComponent(profileVersions[i]));
				}
				for (int i = 0; i < localVersions.length; i++) {
					versionSelector.addVersion(new LocalVersionComponent(localVersions[i]));
				}
				
				/* Remote profiles commented out, due to Skiphs information about them:
				 * 
				 * "I'm afraid I don't know where the source is for the other half of it 
				 * anymore, but the original idea was to use a Forge mod to make the biome 
				 * map generation calls, and then send the data back to AMIDST. While it 
				 * shouldn't be too hard, it's certainly not a perfect system."
				 
				versionSelector.addVersion(new RemoteVersionComponent());
				*/
				
				if (selectedProfile != null) {
					versionSelector.select(selectedProfile);
				}
				
				pack();
				try {
					Thread.sleep(100);
				} catch (InterruptedException ignored) { }
				pack();
			}
		})).start();
		
		versionSelector.setEmptyMessage("Scanning...");
		
		JScrollPane scrollPane = new JScrollPane(versionSelector);
		scrollPane.getVerticalScrollBar().setUnitIncrement(12);
		// The preferred width should be at least a scrollbar-width wider than  
		// the VersionComponent's preferredSize width 500 (so 520?).
		// The preferred height should allow the dialog to fit easily on a 720p
		// display, while being nicely divisible by VersionComponent's height
		// of 40 (so 520 again then?).
		add(scrollPane, "grow, push, w :520:, h 80:520:"); 
		pack();
		setLocation(200, 200);
		setVisible(true);
		
		addKeyListener(versionSelector);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
				System.exit(0);
			}
		});
	}
	
	public static VersionSelectWindow get() {
		return instance;
	}
}
