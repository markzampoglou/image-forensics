
Copyright (C) 2011 Pasquale Ferrara, Tiziano Bianchi, Alessia De Rosa and Alessandro Piva,       
Dipartimento di Elettronica e Telecomunicazioni - Università di Firenze                        
via S. Marta 3 - I-50139 - Firenze, Italy                   

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.


***************
* Description *
***************  
  
this archive includes a Matlab implementation of an algorithm to
detect the presence/absence of CFA artifacts to localize forged region 
as described in P. Ferrara, T. Bianchi, A. De Rosa and P. Piva,
"Image Forgery Localization via Fine-Grained Analysis of CFA Artifacts",  
IEEE Transactions on Information Forensics & Security, vol. 7,  no. 5,  
 Oct. 2012 (published online June 2012),  pp. 1566-1577. 

************
* Platform *
************      

Matlab 7.4.0 (R2007a) (should work also on newer versions)


**************
*Environment *   
**************

tested on Windows XP x64 and Linux. 


************************
*Component Description *
************************

demo.m: demo script
CFAloc.m: baseline algorithm
EMGaussianZM.m: Expectation-Maximization algorithm with a zero-forced component
MoGEstimationZM.m: Estimate model parameter
gaussian_window.m: generate a Gaussian window of 7x7 dimensions
getFeature.m: calculate the proposed feature
getMap.m: generate log-likelihood map
getVarianceMap.m: generate local variances map
likelihood.m: calculate likelihood map
prediction.m: prediction with a bilinear kernel
flowers.tif: tiff untampered image
flowers-tampered.tif: tiff tampered image
garden.jpg: JPEG untampered image
garden-tampered.jpg: JPEG tampered image                     
README.txt: this file
gpl.txt: GPL license


***********************
* Set-up Instructions *  
***********************

extract all files to a single directory. Add the directory to the Matlab path, or set it as the current Matlab directory


********************
* Run Instructions *
********************      

run demo.m from Matlab prompt


**********************
* Output Description *
**********************

the output show the original and the tampered image, the map to localize the forged regions, and the histogram of the proposed feature. 
This latter shows how the feature complies with the model. The algorithm is applied to TIFF and JPEG (quality 100%) images.


***********************
* Contact Information *
***********************

pasquale.ferrara@unifi.it