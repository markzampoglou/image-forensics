function [ Out_Im ] = bilinInterp( CFAIm,BinFilter,CFA )
% Copyright (C) 2016 Markos Zampoglou
% Information Technologies Institute, Centre for Research and Technology Hellas
% 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece

MaskMin=1/4*[1 2 1;2 4 2;1 2 1];
MaskMaj=1/4*[0 1 0;1 4 1;0 1 0];

if ~isempty(find(diff(CFA)==0)) || ~isempty(find(diff(CFA')==0))
    MaskMaj=MaskMaj.*2;
end

Mask=repmat(MaskMin,[1,1,3]);
[a,Maj]=max(sum(sum(BinFilter)));
Mask(:,:,Maj)=MaskMaj;

Out_Im=zeros(size(CFAIm));

for ii=1:3
    Mixed_im=zeros([size(CFAIm,1),size(CFAIm,2)]);
    Orig_Layer=CFAIm(:,:,ii);
    Interp_Layer=imfilter(Orig_Layer,Mask(:,:,ii));
    Mixed_im(BinFilter(:,:,ii)==0)=Interp_Layer(BinFilter(:,:,ii)==0);
    Mixed_im(BinFilter(:,:,ii)==1)=Orig_Layer(BinFilter(:,:,ii)==1);
    Out_Im(:,:,ii)=Mixed_im;
end

Out_Im=uint8(Out_Im);