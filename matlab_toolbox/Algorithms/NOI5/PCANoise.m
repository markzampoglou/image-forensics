function [output, output_quant] = PCANoise(imIn)
    
    % Copyright (C) 2018 Hui Zeng, zengh5@mail2.sysu.edu.cn
    % Southwest University of Science and Technology(SWUST��
    % The code is for academic discussion only, please find detail in our paper
    % [1] H. Zeng, Y. Zhan, X. Kang, X. Lin, Image splicing localization using PCA-based noise 
    % level estimation, Multimedia Tools & Applications, 2017.76(4):4783
    % 2018/08  All the notes are tranlated from Chinese to English

    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    B = 64;
    %  The following 4 images are used in the original paper.  
    %  imx = 'canong3_canonxt_sub_01.tif';
    % imx = 'canong3_nikond70_sub_05.tif';  %2
    %  imx = 'canong3_kodakdcs330_sub_28.tif'; %3
    %imx = 'canonxt_kodakdcs330_sub_10.tif';

    %  Use the same demo image as Zampoglou. 
    % imx = 'demo.tif'; 
    % I = double(rgb2gray(imread(imx)));
        I = double(rgb2gray(imIn));
        [M N] = size(I);
       I = I(1:floor(M/B)*B,1:floor(N/B)*B);
       [M N] = size(I);
       im = I;
    % ensure the image size can be divided by 64
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%% results for 64*64 block  
     for i = 1 : M/B
          for j = 1 : N/B
             Ib = I((i-1)*B+1:i*B,(j-1)*B+1:j*B);
             [label64(i,j), Noise_64(i,j)] =  PCANoiseLevelEstimator(Ib,5);
          end
     end
     %%%%%%%%%%%%%%
      [u re]  = KMeans(Noise_64(:),2);
    result4 = (reshape(re(:,2),size(Noise_64)));
    % dethighlightHZ(im,B,result4');
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% results for 32*32 block 
    B = 32;
      for i = 1 : M/B
          for j = 1 : N/B
             Ib = I((i-1)*B+1:i*B,(j-1)*B+1:j*B);
             [label32(i,j), Noise_32(i,j)] =  PCANoiseLevelEstimator(Ib,5);
          end
      end
       MEDNoise_32= medfilt2(Noise_32,[5 5],'symmetric');
      Noise_32(label32==1)= MEDNoise_32(label32==1);
      [u re]=KMeans(Noise_32(:),2);
    result2=(reshape(re(:,2),size(Noise_32)));
    % dethighlightHZ(im,B,result2');
      %%%%%%%%%% Weighted result of 64*64 and 32*32
      for i = 1 : M/64
          for j = 1 : N/64
                Noise_mix((2*i-1):(2*i),(2*j-1):(2*j)) = Noise_64(i,j);
                initialdetected((2*i-1):(2*i),(2*j-1):(2*j)) = result4(i,j);
          end
      end
       Noise_mix = 0.8*Noise_mix+0.2*Noise_32(1:2*i,1:2*j);        %(4)

    %%%%%%%%%%%%%%%%% only B=32 result is used along the suspicious boundary
    % Note: This is one of the innovations of our work!
    Noise_mix2 = Noise_mix;
    DL = initialdetected(2:end-1,1:end-2) - initialdetected(2:end-1,2:end-1);
    DR = initialdetected(2:end-1,2:end-1) - initialdetected(2:end-1,3:end);
    DU = initialdetected(1:end-2,2:end-1) - initialdetected(2:end-1,2:end-1);
    DD = initialdetected(2:end-1,2:end-1) - initialdetected(3:end,2:end-1);
    Edge = zeros(size(initialdetected));
    Edge(2:end-1,2:end-1)= abs(DL)+abs(DR)+abs(DU)+abs(DD);
    g = (Edge>0);
    Noise_mix2(g) = Noise_32(g);
    %figure,imagesc(Noise_mix2)
    % If only qualitative results is needed, Noise_mix2 is ennough
    % The following code is for quantization evaluation
    %%%%%%%%%%%%%%%  K-means
     [u re]=KMeans(Noise_mix2(:),2);
    result4=(reshape(re(:,2),size(Noise_mix2)));
    % dethighlightHZ(im,B,result4');
    %%%%%%%%%%%%%%%%%%  ignore isolate patch
    bwpp = bwlabel(result4-1);
    area = regionprops(bwpp,'area');
    for num =1: length(area)
                     if area(num,1).Area < 4
                         result4(bwpp==num)=1;
                     end
    end
    output=Noise_mix2;
    bwpp = bwlabel(result4-1);
    output_quant=bwpp;
    %dethighlightHZ(I,B,result4');