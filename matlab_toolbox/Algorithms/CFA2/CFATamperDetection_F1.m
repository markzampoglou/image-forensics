function [F1Map,CFADetected, F1] = CFATamperDetection_F1(im)
    % This is an implementation of the first of the two algorithms
    % presented in Dirik, Ahmet Emir, and Nasir D. Memon. "Image tamper
    % detection based on demosaicing artifacts." In ICIP, pp. 1497-1500.
    % 2009.
    
    % Copyright (C) 2016 Markos Zampoglou
    % Information Technologies Institute, Centre for Research and Technology Hellas
    % 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece
    
    StdThresh=5;
    Depth=3;
    
    im=double(im(1:round(floor(end/(2^Depth))*(2^Depth)),1:round(floor(end/(2^Depth))*(2^Depth)),:));
    
    SmallCFAList={[2 1;3 2] [2 3;1 2] [3 2;2 1] [1 2;2 3]};
    LargeCFAList={ [1 2;2 3] [3 2;2 1] [2 1;3 2]  [2 3;1 2] [1 2; 3 1] [1 3;2 1] [2 1;1 3] [3 1;1 2] [3 1;2 3] [3 2;1 3] [1  3;3 2] [2 3;3 1]                  [1 2;1 3] [1 3;1 2] [1 1;2 3] [1 1;3 2] [2 1;3 1] [3 1;2 1] [2 3;1 1] [3 2;1 1] [3 1;3 2] [3 2;3 1] [3 3;1 2] [3 3;2 1] [1 3;2 3] [2 3;1 3] [1 2;3 3] [2 1;3 3]                                                   [2 1;2 3] [2 3;2 1] [2 2;1 3] [2 2;3 1]  [1 2;3 2] [3 2;1 2] [1 3;2 2] [3 1;2 2]};
    
    CFAList=SmallCFAList;
    
    %block size
    W1=16;
    
    if size(im,1)<W1 || size(im,2)<W1
        F1Map=zeros([size(im,1), size(im,2)]);
        CFADetected=[0 0 0 0];
        return
    end
    
    MeanError=inf(length(CFAList),1);
    for TestArray=1:length(CFAList)
        
        BinFilter=[];
        ProcIm=[];
        CFA=CFAList{TestArray};
        R=CFA==1;
        G=CFA==2;
        B=CFA==3;
        BinFilter(:,:,1)=repmat(R,size(im,1)/2,size(im,2)/2);
        BinFilter(:,:,2)=repmat(G,size(im,1)/2,size(im,2)/2);
        BinFilter(:,:,3)=repmat(B,size(im,1)/2,size(im,2)/2);
        CFAIm=double(im).*BinFilter;
        BilinIm=bilinInterp(CFAIm,BinFilter,CFA);
        
        
        ProcIm(:,:,1:3)=im;
        ProcIm(:,:,4:6)=double(BilinIm);
        
        ProcIm=double(ProcIm);
        BlockResult=blockproc(ProcIm,[W1 W1],@eval_block);
        
        Stds=BlockResult(:,:,4:6);
        BlockDiffs=BlockResult(:,:,1:3);
        NonSmooth=Stds>StdThresh;
        
        MeanError(TestArray)=mean(mean(mean(BlockDiffs(NonSmooth))));
        BlockDiffs=BlockDiffs./repmat(sum(BlockDiffs,3),[1 1 3]);
        
        Diffs(TestArray,:)=reshape(BlockDiffs(:,:,2),1,numel(BlockDiffs(:,:,2)));
        F1Maps{TestArray}=BlockDiffs(:,:,2);
    end
    
    Diffs(isnan(Diffs))=0;
    
    [~,val]=min(MeanError);
    U=sum(abs(Diffs-0.25),1);
    F1=median(U);
    CFADetected=CFAList{val}==2;
    F1Map=F1Maps{val};
    
end

function [ Out ] = eval_block( block_struc )
    im=block_struc.data;
    Out(:,:,1)=mean2((double(block_struc.data(:,:,1))-double(block_struc.data(:,:,4))).^2);
    Out(:,:,2)=mean2((double(block_struc.data(:,:,2))-double(block_struc.data(:,:,5))).^2);
    Out(:,:,3)=mean2((double(block_struc.data(:,:,3))-double(block_struc.data(:,:,6))).^2);
    
    Out(:,:,4)=std(reshape(im(:,:,1),1,numel(im(:,:,1))));
    Out(:,:,5)=std(reshape(im(:,:,2),1,numel(im(:,:,2))));
    Out(:,:,6)=std(reshape(im(:,:,3),1,numel(im(:,:,3))));
end