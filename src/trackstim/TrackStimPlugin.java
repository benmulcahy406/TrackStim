/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trackstim;

import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

import org.micromanager.MenuPlugin;
import org.micromanager.Studio;


@Plugin(type = MenuPlugin.class)
public class TrackStimPlugin implements SciJavaPlugin, MenuPlugin {
   public TrackStimController tsc;
   public Studio studio;

   @Override
   public void setContext(Studio studio_) {
      studio = studio_;
   }

   /**
    * This method is called when the plugin's menu option is selected.
    */
   @Override
   public void onPluginSelected() {
      tsc = new TrackStimController(studio.core());
      tsc.gui.setVisible(true);
   }

   /**
    * This method determines which sub-menu of the Plugins menu we are placed
    * into.
    */
   @Override
   public String getSubMenu() {
      return "";
      // Indicates that we should show up in the root Plugins menu.
   }

   @Override
   public String getName() {
      return "TrackStim";
   }

   @Override
   public String getHelpText() {
      return "Imaging system";
   }

   @Override
   public String getVersion() {
      return "0.0.1";
   }

   @Override
   public String getCopyright() {
      return "Zhen Lab, 2019";
   }
}