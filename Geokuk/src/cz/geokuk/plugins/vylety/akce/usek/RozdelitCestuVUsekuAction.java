/**
 * 
 */
package cz.geokuk.plugins.vylety.akce.usek;


import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.vylety.cesty.Usek;

/**
 * Jde na vybranou pozici
 * @author veverka
 *
 */
public class RozdelitCestuVUsekuAction extends UsekAction0 {

  private static final long serialVersionUID = -2882817111560336824L;

  //  private Pozice pozice;
  /**
   * @param aBoard
   */
  public RozdelitCestuVUsekuAction(Usek usek, Mou mouMysi) {
    super(usek, mouMysi);
    putValue(NAME, "Rozdělit cestu");
    putValue(SHORT_DESCRIPTION, "Rozdělí cestu na dvě cesty kratší.");
    //putValue(MNEMONIC_KEY, KeyEvent.VK_P);
    //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("RIGHT"));
  }

  @Override
  protected boolean mamPovolitProUsek(Usek usek, Mou mou) {
    return true;
  }

  @Override
  protected void nastavJmenoAkce(Usek usek, Mou mou) {
    putValue(NAME, "<html>Rozdělit cestu" + usek.getCesta().getNazevHtml() + " " + usek.dalkaCestaRozdelenoHtml(mou));
  }

  @Override
  protected void provedProUsek(Usek usek, Mou mou) {
    vyletModel.rozdelCestuVUseku(usek, mou, false);
  }

}
