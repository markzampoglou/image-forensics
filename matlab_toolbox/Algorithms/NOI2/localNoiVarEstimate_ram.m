function [V] = localNoiVarEstimate_ram(noisyIm,filter_type,filter_size,block_rad) 
% localNoiVarEstimate: local noise variance estimation using kurtosis
%
% [estVar] = localNoiVarEstimate(noisyIm,filter_type,filter_size,block_size)
% 
% input arguments:
%	noisyIm: input noisy image 
%	filter_type: the type of band-pass filter used 
%        supported types, "dct", "haar", "rand"
%   filter_size: the size of the support of the filter
%   block_rad: the size of the local blocks
% output arguments:
%	estVar: estimated local noise variance
%
% reference:
%   X.Pan, X.Zhang and S.Lyu, Exposing Image Splicing with
%   Inconsistent Local Noise Variances, IEEE International
%   Conference on Computational Photography, Seattle, WA, 2012
% 
% disclaimer:
%	Please refer to the ReadMe.txt
%
% Xunyu Pan, Xing Zhang and Siwei Lyu -- 07/26/2012             

switch filter_type
	case 'dct',
		fltrs = dct2mtx(filter_size,'snake');
	case 'haar',
		fltrs = haar2mtx(filter_size);
	case 'rand',
		fltrs = rnd2mtx(filter_size);
	otherwise,
		error('unknown filter');
end

% decompose into channels
ch = zeros([size(noisyIm),filter_size*filter_size-1],'single');
for k = 2:(filter_size*filter_size)
	ch(:,:,k-1) = conv2(noisyIm,fltrs(:,:,k),'same');
end

% collect raw moments
blksz = (2*block_rad+1)*(2*block_rad+1);
mu1 = block_avg(ch,block_rad,'mi');
mu2 = block_avg(ch.^2,block_rad,'mi');
mu3 = block_avg(ch.^3,block_rad,'mi');
mu4 = block_avg(ch.^4,block_rad,'mi');

% variance & sqrt of kurtosis
noiV = mu2 - mu1.^2;
noiK = (mu4 - 4*mu1.*mu3 + 6*mu1.^2.*mu2 - 3*mu1.^4)./(noiV.^2)-3; 
noiK = max(0,noiK);

a = mean(sqrt(noiK),3);
b = mean(1./noiV,3);
c = mean(1./noiV.^2,3);
d = mean(sqrt(noiK)./noiV,3);
e = mean(noiV,3);

sqrtK = (a.*c - b.*d)./(c-b.*b);

V = single((1 - a./sqrtK)./b);
idx = sqrtK<median(sqrtK(:));
V(idx) = 1./b(idx);
idx = V<0;
V(idx) = 1./b(idx);

return