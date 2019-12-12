package cz.geokuk.plugins.kesoid.mapicon;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.api.mapicon.Drawer0;
import cz.geokuk.plugins.kesoid.genetika.Alela;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;

public class IconDefNacitac {

	/**
	 *
	 */

	private static final Logger log = LogManager.getLogger(IconDefNacitac.class.getSimpleName());

	// TODO : The alelas should have a more generic name
	private static Pattern pat = Pattern.compile("([a-z0-9]+!)*([^_]*)((?:_[ěščřžýáíéóúůďťňĎŇŤŠČŘŽÝÁÍÉÚŮa-zA-z -]+)*)(_x-?[0-9]+)*(_y-?[0-9]+)*(_p[0-9])*\\.([a-z]+)");

	private final String jmenoSPriponou;
	// private IconDef iconDef;
	IkonDrawingProperties idp;
	private final URL url;

	private final Genom genom;
	private final IkonNacitacSada iIkonNacitacSada;

	public IconDefNacitac(final Genom genom, final String jmenoSPriponou, final URL url, final IkonNacitacSada aIkonNacitacSada) {
		this.jmenoSPriponou = jmenoSPriponou;
		this.url = url;
		this.genom = genom;
		iIkonNacitacSada = aIkonNacitacSada;
	}

	public IconDef loadIconDef(final ImageProvider imageProvider) {
		try {
			return load(imageProvider);
		} catch (final IOException e) {
			FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Selhalo čtení obrázku ikony, tak obrázek nemůžeme použít");
			log.error("Selhalo čtení obrázku ikony, tak obrázek nemůžeme použít", e);
			return null;
		}
	}

	private boolean isProperties(final String sufix) {
		return sufix.equals("properties");
	}

	private IconDef load(final ImageProvider imageProvider) throws IOException {
		idp = new IkonDrawingProperties();
		idp.url = url;
		final String machovanec = jmenoSPriponou.startsWith("_.") ? jmenoSPriponou.substring(1) : jmenoSPriponou;
		final Matcher mat = pat.matcher(machovanec);
		if (!mat.matches()) {
			log.error("Jméno \"" + jmenoSPriponou + "\" nevyhovuje regulárnímu výrazu: " + pat);
			return null;
		}
		final String grupaName = mat.group(1);

		final String wptsym = odstranZazavorkovaneNesouboroviteZnaky(mat.group(2));

		final Set<Alela> alely = nactiAlely(mat.group(3));
		Alela alelaSym = null;
		if (wptsym != null && wptsym.trim().length() > 0) {
			//alelaSym = genom.alelaSym(wptsym, grupaName, iIkonNacitacSada.getGroupDisplayName(grupaName));
			alelaSym = genom.gen(grupaName.substring(0, grupaName.length()-1)).alela(wptsym);
			System.out.println("MAMEGEN: " + alelaSym);
			alely.add(alelaSym);
		}
		// rozmnoz(alely, sese);
		final String sufix = mat.group(7);
		if (isProperties(sufix)) {
			nactiObrazekDefinovanyVPropertach();
		} else { // zkusíme to považovat za obrázek
			otestujAVydefinujSkutecnyObrazek(imageProvider);
		}

		idp.xoffset = zpracujNaOffsete(mat.group(4));
		idp.yoffset = zpracujNaOffsete(mat.group(5));
		final int priorita = zpracujNaPrioritu(mat.group(6));

		final IconDef iconDef = new IconDef();
		iconDef.setAlelyx(alely);
		iconDef.setSelektivniAlela(alelaSym); // nastavit, aby se podle ní dalo rychle filtrovat, může být i null, pak je to bez alely a filtruje se vždy.
		iconDef.idp = idp;
		iconDef.priorita = priorita;

		return iconDef;
	}

	private Set<Alela> nactiAlely(final String alelygroup) {
		final Set<Alela> alely = new HashSet<>();
		for (final String sa : alelygroup.split("_")) {
			if (sa.isEmpty()) {
				continue;
			}
			Alela alela;
			final String s = LegacyAlelaNames.preloz(sa); // přeložíme staré názvy na nové kvalifikované
			final int pozminus = s.indexOf('-');
			if (pozminus < 0) { // žádné mínus, alela musí existovat
				throw new IllegalArgumentException("Nemůe nastat, mínus tam dává překlad vždy");
			} else {
				final String alelaName = s.substring(pozminus + 1);
				final String genName = s.substring(0, pozminus);
				alela = genom.gen(genName).alela(alelaName);
				if (alela == null) {
					continue;
				}
			}
			alely.add(alela);
		}
		return alely;
	}

	private void nactiObrazekDefinovanyVPropertach() throws IOException {
		final Properties prop = new Properties();
		prop.load(new BufferedInputStream(idp.url.openStream()));
		// idp.width = Integer.parseInt(prop.getProperty("width"));
		// idp.height= Integer.parseInt(prop.getProperty("height"));
		idp.properties = prop;
		final String className = prop.getProperty("class");
		try {
			idp.vykreslovac = (Drawer0) Class.forName(className).newInstance();
			naplnVykreslovac(idp.vykreslovac);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param aVykreslovac
	 */
	private void naplnVykreslovac(final Drawer0 vykreslovac) {
		vykreslovac.setIdp(idp);
	}

	/**
	 * @param aGroup
	 * @return
	 */
	private String odstranZazavorkovaneNesouboroviteZnaky(String s) {
		if (s.indexOf('[') < 0) {
			return s;
		}
		s = s.replace("[lomitko]", "/");
		s = s.replace("[hvezdicka]", "*");
		s = s.replace("[uvozovky]", "*");
		s = s.replace("[otaznik]", "*");
		s = s.replace("[dvojtecka]", "*");
		return s;
	}

	private void otestujAVydefinujSkutecnyObrazek(final ImageProvider imageProvider) throws IOException {
		// @SuppressWarnings("unused") // jen pro kontrolu
		// BufferedImage bi = ImageIO.read(idp.url);
		// idp.width = bi.getWidth();
		// idp.height = bi.getHeight();
		idp.properties = new Properties();
		idp.vykreslovac = new DefaultVykreslovac(imageProvider);
		naplnVykreslovac(idp.vykreslovac);
	}

	private int zpracujNaOffsete(final String zadano) {
		if (zadano == null) {
			return 0;
		}
		final int zadanapozice = Integer.parseInt(zadano.substring(2));
		return zadanapozice;
	}

	private int zpracujNaPrioritu(final String zadano) {
		if (zadano == null) {
			return 5;
		}
		return Integer.parseInt(zadano.substring(2));
	}

}
