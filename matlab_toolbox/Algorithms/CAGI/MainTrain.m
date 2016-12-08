 function [MeanContent,MeanStrongEdge]=MainTrain(filenameIm,blk_idx,blk_idy)
%/////////////////////////////////////////////////////////////////////////////////////////////////
%/////////////////////////////////////////////////////////////////////////////////////////////////

R10=imread(filenameIm);
[x,y,z]=size(R10);

[PMasks, MMasks, MaskWhite]=data();
%////////Image Tiling 3 Scales////////////////////////////
tileF=ImageTiling(R10);

%////////////Smaping/////////////////////////////////////
smapF=SmapIng(tileF, PMasks, MaskWhite);

% % % %////////////Filtering///////////////////////////////////
[ThresSmall,ThresBig, ThresImg] =filtering(smapF);

 smapF_filtrOld=filteringMethod (smapF, ThresSmall, ThresBig, ThresImg);

%/////////////PaintEdges/////////////////////////////////
[e,edge,contours]=PaintimgEdges(smapF_filtrOld, MMasks, 1);
Output=imresize(e, [x y],'nearest');

Outputedge=imresize(edge, [x y],'nearest');
StrongEdge=imresize(contours, [x y],'nearest');
for i=1:blk_idx
  for j=1:blk_idy
      a=1+(i-1)*8;
      b=1+(j-1)*8;
      MeanContent(i,j)=mean(mean(Output(a:a+7, b:b+7)));
      MeanStrongEdge(i,j)=mean(mean(StrongEdge(a:a+7, b:b+7)));
  end
end
MeanStrongEdge(MeanStrongEdge>0.5)=1;

MeanStrongEdge(MeanStrongEdge<=0.5)=0;
        






