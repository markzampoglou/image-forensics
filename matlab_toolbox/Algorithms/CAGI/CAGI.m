% Copyright (C) 2016 C. Iakovidou
% Information Technologies Institute, Centre for Research and Technology Hellas
% 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece

function [ Result_CAGI,Result_Inv_CAGI ] = CAGI( filenameIm )

    RGB=double(imread(filenameIm));
    [height,width,color]=size(RGB);

    if (color==3)

         HSV=rgb2hsv(RGB);
         H_im=HSV(:,:,1); 
         S_im=HSV(:,:,2);
         V_im=HSV(:,:,3);

        YCbCr=rgb2ycbcr(RGB);
        pixels=YCbCr(:,:,1);

    else
         RGB2(:,:,1)=RGB;
         RGB2(:,:,2)=RGB;
         RGB2(:,:,3)=RGB;

        HSV=rgb2hsv(RGB2);
        H_im=HSV(:,:,1);
        S_im=HSV(:,:,2);
        V_im=HSV(:,:,3);

        YCbCr=rgb2ycbcr(RGB2);
        pixels=YCbCr(:,:,1);
    end

    if height*width<480*640   
    sgrid=2;   %low resolution images under vga
    else
    sgrid=3;
    end

    bins=40;
    imageGS=pixels;

    [x,y]=size(imageGS);
    blk_idx= floor(x/8-1);
    blk_idy=floor(y/8-1);
    kx=floor(blk_idx/sgrid);
    ky=floor(blk_idy/sgrid);
    BlockScoreALL=zeros(blk_idx,blk_idy,8,8);
    Kscores=zeros(8,8,2);

    for p=1:8
        for q=1:8

            [K,Correct,BlockScoreALL(:,:,p,q)]=inblockpatterns(imageGS,bins,p,q, blk_idx, blk_idy);

            if K>1.999999
            Kscores(p,q,1)=0;
            Kscores(p,q,2)=Correct;
            else
            Kscores(p,q,1)=K;
            Kscores(p,q,2)=Correct;
            end

        end
    end

    % Predict('######################################################################')
     [Kpredict,Kpre]=predict0(Kscores);
     [PossiblePoints]=predict1(Kscores,Kpredict, Kpre);
     PossiblePoints = sortrows(PossiblePoints,7);
    % disp('######################################################################')

     [MeanContent,MeanStrongEdge]=MainTrain(filenameIm,blk_idx,blk_idy);

    for i=1:kx
      for j=1:ky
          a=1+(i-1)*sgrid;
          b=1+(j-1)*sgrid;
          ccc=sgrid-1;
          MeanContent2(i,j)=mean(mean(MeanContent(a:a+ccc, b:b+ccc)));
      end 
    end


    [MeanInSpace,PossiblePoints,diff_Mean_Best_scaled,diff_Mean_Best_scaledInv]=scores_pick_variables(BlockScoreALL,sgrid,blk_idx,blk_idy,PossiblePoints,kx,ky);

    [E,EInv]=characterizeblocks(MeanContent2,MeanStrongEdge, V_im, blk_idx,blk_idy, MeanInSpace,diff_Mean_Best_scaled,diff_Mean_Best_scaledInv,sgrid,PossiblePoints,kx,ky);

    [Result_CAGI]=RescaleToImageResult(E,sgrid,kx,ky,pixels);
    [Result_Inv_CAGI]=RescaleToImageResult(EInv,sgrid,kx,ky,pixels);



end

