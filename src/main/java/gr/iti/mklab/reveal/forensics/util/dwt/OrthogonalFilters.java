package gr.iti.mklab.reveal.forensics.util.dwt;

/**
 * Author Mark Bishop; 2014
 * License GNU v3; 
 * This class is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 		The coefficients were gathered from various sources including:
 *		Wavelab Version 850
 *		Adapted Wavelet Analysis from Theory to Software, Victor Wickerhauser
 *		A Wavelet Tour of Signal Processing, The Sparse Way, 3rd ed., Stephane Mallet
 */

import java.util.ArrayList;

/**
 * Class responsibility: Provide filters for several orthogonal wavelets.
 *
 */

public class OrthogonalFilters {

	/**
	 * 
	 * @param H
	 *            low pass filter for QMF pair
	 * @return high pass filter for QMF pair
	 */
	public static double[] getHighPass(double[] H) {
		double sign = 1;
		int n = H.length;
		double[] G = new double[n];
		for (int i = 0; i < n; i++) {
			G[n - i - 1] = H[i] * sign;
			sign *= -1;
		}
		return G;
	}

	/**
	 * 
	 * @param wavelet To be explained
	 * @param order To be explained
	 * @return Low Pass filter for QMF pair
	 */
	static double[] getLowPass(Wavelet wavelet, int order) {
		if (wavelet == Wavelet.Haar) {
			double[] f = { .707106781186547, .707106781186547 };
			return f;
		}
		if (wavelet == Wavelet.Daubechies) {
			switch (order) {
			case 4: { // (Wickerhauser)
				double[] f = { .482962913144534160, .836516303737807940,
						.224143868042013390, -.129409522551260370 };
				return f;

			}
			case 6: {// (Wickerhauser)
				double[] f = { 0.332670552950082630, 0.806891509311092550,
						0.459877502118491540, -0.135011020010254580,
						-0.0854412738820266580, 0.0352262918857095330 };
				return f;

			}
			case 8: {// (Wickerhauser but order reversed)
				double[] f = { 0.2303778133090, 0.7148465705530,
						0.6308807679300, -0.02798376941700, -0.1870348117190,
						0.03084138183700, 0.032883011667, -0.01059740178500 };

				return f;

			}
			case 10: {// (Wickerhauser but order reversed)
				double[] f = { 0.160102397974, 0.603829269797, 0.724308528438,
						0.138428145901, -0.242294887066, -0.032244869585,
						0.07757149384, -0.006241490213, -0.012580751999,
						0.003335725285 };
				return f;

			}
			case 12: {// (Wickerhauser but order reversed)
				double[] f = { 0.11154074335, 0.494623890398, 0.751133908021,
						0.315250351709, -0.226264693965, -0.129766867567,
						0.097501605587, 0.02752286553, -0.031582039318,
						0.000553842201, 0.004777257511, -0.001077301085 };
				return f;

			}
			case 14: {// (Wickerhauser but order reversed)
				double[] f = { 0.077852054085, 0.396539319482, 0.729132090846,
						0.469782287405, -0.143906003929, -0.224036184994,
						0.071309219267, 0.080612609151, -0.038029936935,
						-0.016574541631, 0.012550998556, 0.000429577973,
						-0.001801640704, 0.0003537138 };
				return f;

			}
			case 16: {// (Wickerhauser but order reversed)
				double[] f = { 0.054415842243, 0.312871590914, 0.675630736297,
						0.585354683654, -0.015829105256, -0.284015542962,
						0.000472484574, 0.12874742662, -0.017369301002,
						-0.044088253931, 0.013981027917, 0.008746094047,
						-0.004870352993, -0.000391740373, 0.000675449406,
						-0.000117476784 };
				return f;

			}
			case 18: {// (Wickerhauser but order reversed)
				double[] f = { 0.038077947364, 0.243834674613, 0.60482312369,
						0.657288078051, 0.133197385825, -0.293273783279,
						-0.096840783223, 0.148540749338, 0.030725681479,
						-0.067632829061, 0.000250947115, 0.022361662124,
						-0.004723204758, -0.004281503682, 0.001847646883,
						0.000230385764, -0.000251963189, 0.00003934732 };
				return f;

			}
			case 20: {// Wavelab src
				double[] f = { 0.026670057901, 0.188176800078, 0.527201188932,
						0.688459039454, 0.281172343661, -0.249846424327,
						-0.195946274377, 0.127369340336, 0.093057364604,
						-0.071394147166, -0.029457536822, 0.033212674059,
						0.003606553567, -0.010733175483, 0.001395351747,
						0.001992405295, -0.000685856695, -0.000116466855,
						0.00009358867, -0.000013264203 };
				return f;

			}
			}
		}
		if (wavelet == Wavelet.Beylkin) { // 18 (Wickerhauser but order
											// reversed)

			double[] f = { 0.00064048532852124535, -0.00273603162625860610,
					0.0014842347824723461, 0.01004041184463199,
					-0.014365807968852611, -0.017460408696028829,
					0.042916387274192273, 0.01967986604432212,
					-0.088543630623924835, -0.0175207462665229649,
					0.1555387318770938, 0.02690030880369032,
					-0.26449723144638482, -0.1109275983482343,
					0.44971825114946867, 0.69982521405660590,
					0.42421536081296141, 0.099305765374353927 };

			return f;

		}
		if (wavelet == Wavelet.Vaidyanathan) {// length = 24 // Wavelab src
			double[] f = { -.000062906118, .000343631905, -.000453956620,
					-.000944897136, .002843834547, .000708137504,
					-.008839103409, .003153847056, .019687215010,
					-.014853448005, -.035470398607, .038742619293,
					.055892523691, -.077709750902, -.083928884366,
					.131971661417, .135084227129, -.194450471766,
					-.263494802488, .201612161775, .635601059872,
					.572797793211, .250184129505, .045799334111 };
			return f;
		}
		if (wavelet == Wavelet.Coiflet) {// Wavelab src
			switch (order) {
			case 6: {
				double[] f = { .038580777748, -.126969125396, -.077161555496,
						.607491641386, .745687558934, .226584265197 };
				return f;
			}
			case 12: {
				double[] f = { .016387336463, -.041464936782, -.067372554722,
						.386110066823, .812723635450, .417005184424,
						-.076488599078, -.059434418646, .023680171947,
						.005611434819, -.001823208871, -.000720549445 };
				;
				return f;
			}
			case 18: {
				double[] f = { -.003793512864, .007782596426, .023452696142,
						-.065771911281, -.061123390003, .405176902410,
						.793777222626, .428483476378, -.071799821619,
						-.082301927106, .034555027573, .015880544864,
						-.009007976137, -.002574517688, .001117518771,
						.000466216960, -.000070983303, -.000034599773 };

				return f;

			}
			case 24: {
				double[] f = { .000892313668, -.001629492013, -.007346166328,
						.016068943964, .026682300156, -.081266699680,
						-.056077313316, .415308407030, .782238930920,
						.434386056491, -.066627474263, -.096220442034,
						.039334427123, .025082261845, -.015211731527,
						-.005658286686, .003751436157, .001266561929,
						-.000589020757, -.000259974552, .000062339034,
						.000031229876, -.000003259680, -.000001784985 };
				return f;

			}
			case 30: {
				double[] f = { -.000212080863, .000358589677, .002178236305,
						-.004159358782, -.010131117538, .023408156762,
						.028168029062, -.091920010549, -.052043163216,
						.421566206729, .774289603740, .437991626228,
						-.062035963906, -.105574208706, .041289208741,
						.032683574283, -.019761779012, -.009164231153,
						.006764185419, .002433373209, -.001662863769,
						-.000638131296, .000302259520, .000140541149,
						-.000041340484, -.000021315014, .000003734597,
						.000002063806, -.000000167408, -.000000095158 };
				return f;

			}
			}
		}
		if (wavelet == Wavelet.Symmlet) {// Wavelab but normalized by 1/sqrt(2)
			switch (order) {

			case 4: {
				double[] f = { -0.07576571478935668, -0.029635527645960395,
						0.4976186676325629, 0.803738751805386,
						0.29785779560560505, -0.09921954357695636,
						-0.012603967262263831, 0.03222310060407815 };

				return f;

			}
			case 5: {
				double[] f = { 0.027333068345162827, 0.029519490926071618,
						-0.039134249302581074, 0.19939753397698343,
						0.7234076904037638, 0.6339789634569107,
						0.01660210576442332, -0.17532808990809687,
						-0.021101834024928552, 0.019538882735385715 };

				return f;

			}
			case 6: {
				double[] f = { 0.0154041093273385, 0.003490712084330607,
						-0.11799011114841682, -0.04831174258600069,
						0.49105594192766583, 0.7876411410288363,
						0.3379294217282582, -0.07263752278660392,
						-0.021060292512696543, 0.04472490177075063,
						0.0017677118643983668, -0.007800708324765445 };

				return f;

			}
			case 7: {
				double[] f = { 0.00268181456811643, -0.0010473848889657486,
						-0.01263630340315281, 0.0305155131659067,
						0.06789269350159828, -0.049552834937041906,
						0.01744125508711021, 0.5361019170907816,
						0.7677643170045847, 0.28862963175098844,
						-0.14004724044270553, -0.10780823770361868,
						0.004010244871703328, 0.01026817670849701 };

				return f;

			}
			case 8: {
				double[] f = { 0.0018899503329009007, -3.0292051455166417E-4,
						-0.014952258336793822, 0.0038087520140603582,
						0.04913717967348071, -0.027219029916815855,
						-0.05194583810787925, 0.36444189483598555,
						0.77718575169981, 0.48135965125923963,
						-0.06127335906791368, -0.1432942383510657,
						0.0076074873252853755, 0.03169508781034778,
						-5.4213233163559E-4, -0.0033824159513597117 };

				return f;

			}
			case 9: {
				double[] f = { 0.0010694900326524929, -4.7315449858729915E-4,
						-0.010264064027672312, 0.00885926749350085,
						0.062077789302687425, -0.018233770779803246,
						-0.19155083129625178, 0.03527248803589115,
						0.6173384491405931, 0.717897082763343,
						0.23876091460712456, -0.054568958430509365,
						5.834627463304675E-4, 0.03022487885795232,
						-0.011528210207970613, -0.013271967781516928,
						6.197808890541263E-4, 0.0014009155255699105 };

				return f;

			}
			case 10: {
				double[] f = { 7.70159808941683E-4, 9.563267076371025E-5,
						-0.008641299274130404, -0.0014653825830396562,
						0.045927239214146896, 0.011609893910541074,
						-0.1594942788241296, -0.07088053579601779,
						0.4716906667438779, 0.7695100368531889,
						0.3838267611450017, -0.035536740298026795,
						-0.03199005682146376, 0.049994972068686125,
						0.005764912044344502, -0.02035493979968329,
						-8.043589343685685E-4, 0.004593173582708373,
						5.703608432707147E-5, -4.5932942045186415E-4 };

				return f;
			}
			}
		}

		// Battle performs comparatively poorly using the coefficients below
		// (sig figs?).
		if (wavelet == Wavelet.Battle) {
			switch (order) {

			case 1: {
				double[] f = { -0.000122686, -0.000224296, 0.000511636,
						0.000923371, -0.002201945, -0.003883261, 0.009990599,
						0.016974805, -0.051945337, -0.06910102, 0.39729643,
						0.817645956, 0.39729643, -0.06910102, -0.051945337,
						0.016974805, 0.009990599, -0.003883261, -0.002201945,
						0.000923371, 0.000511636, -0.000224296, -0.000122686 };
				return f;

			}
			case 3: {
				double[] f = { 0.000146098, -0.000232304, -0.000285414,
						0.000462093, 0.000559952, -0.000927187, -0.001103748,
						0.00188212, 0.002186714, -0.003882426, -0.00435384,
						0.008201477, 0.008685294, -0.017982291, -0.017176331,
						0.042068328, 0.032080869, -0.110036987, -0.050201753,
						0.433923147, 0.766130398, 0.433923147, -0.050201753,
						-0.110036987, 0.032080869, 0.042068328, -0.017176331,
						-0.017982291, 0.008685294, 0.008201477, -0.00435384,
						-0.003882426, 0.002186714, 0.00188212, -0.001103748,
						-0.000927187, 0.000559952, 0.000462093, -0.000285414,
						-0.000232304, 0.000146098 };
				return f;
			}
			}

		}
		double[] error = { 0 };
		return error;
	}

	/**
	 * 
	 * @param wavelet To be explained
	 * @return List of available orders for a given wavelet.
	 */
	public static ArrayList<Integer> validParameters(Wavelet wavelet) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		switch (wavelet) {
		case Haar:
			result.add(2);
			break;
		case Daubechies:
			for (int i = 4; i <= 20; i += 2) {
				result.add(i);
			}
			break;
		case Beylkin:
			result.add(18);
			break;
		case Vaidyanathan:
			result.add(24);
			break;
		case Coiflet:
			for (int i = 6; i <= 30; i += 6) {
				result.add(i);
			}
			break;
		case Symmlet:
			for (int i = 4; i <= 10; i++) {
				result.add(i);
			}
			break;
		case Battle:
			result.add(1);
			result.add(3);
			break;
		}
		return result;
	}

	/**
	 * 
	 * @param parameter To be explained
	 * @param wavelet To be explained
	 * @param signalLength To be explained
	 * @return A list of valid scales for a given wavelet and signal length.
	 * @throws Exception
	 */
	public static ArrayList<Integer> validScales(int parameter,
			Wavelet wavelet, int signalLength) throws Exception {
		ArrayList<Integer> validScales = new ArrayList<Integer>();
		int hLength = OrthogonalFilters.getLowPass(wavelet, parameter).length;
		if (hLength > signalLength) {
			throw new Exception("The filter is longer than the signal.");
		}
		int log2SigLen = (int) (Math.log(signalLength) / Math.log(2));
		int min = (int) (Math.log(hLength) / Math.log(2));
		for (int i = min; i <= log2SigLen; i++) {
			double t = Math.log(hLength);
			if (t < min) {
				validScales.add(i);
			}
		}
		return validScales;
	}
}

// Some descriptive narrative from the Wavelab project:
//
// Description
// The Haar filter (which could be considered a Daubechies-2) was the
// first wavelet, though not called as such, and is discontinuous.
//
// The Beylkin filter places roots for the frequency response function
// close to the Nyquist frequency on the real axis.
//
// The Coiflet filters are designed to give both the mother and father
// wavelets 2*Par vanishing moments; here Par may be one of 1,2,3,4 or 5.
//
// The Daubechies filters are minimal phase filters that generate wavelets
// which have a minimal support for a given number of vanishing moments.
// They are indexed by their length, Par, which may be one of
// 4,6,8,10,12,14,16,18 or 20. The number of vanishing moments is par/2.
//
// Symmlets are also wavelets within a minimum size support for a given
// number of vanishing moments, but they are as symmetrical as possible,
// as opposed to the Daubechies filters which are highly asymmetrical.
// They are indexed by Par, which specifies the number of vanishing
// moments and is equal to half the size of the support. It ranges
// from 4 to 10.
//
// The Vaidyanathan filter gives an exact reconstruction, but does not
// satisfy any moment condition. The filter has been optimized for
// speech coding.
//
// The Battle-Lemarie filter generate spline orthogonal wavelet basis.
// The order Par gives the degree of the spline. The number of
// vanishing moments is Par+1.
//

//
