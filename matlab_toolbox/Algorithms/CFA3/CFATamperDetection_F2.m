function [F2Map, CFAOut]=CFATamperDetection_F2(im)
    
    StdThresh=5;
    Depth=3;
    
    im=double(im(1:round(floor(end/(2^Depth))*(2^Depth)),1:round(floor(end/(2^Depth))*(2^Depth)),:));
    
    SmallCFAList={[2 1;3 2] [2 3;1 2] [3 2;2 1] [1 2;2 3]};
    %LargeCFAList={ [1 2;2 3] [3 2;2 1] [2 1;3 2]  [2 3;1 2] [1 2; 3 1] [1 3;2 1] [2 1;1 3] [3 1;1 2] [3 1;2 3] [3 2;1 3] [1  3;3 2] [2 3;3 1]                  [1 2;1 3] [1 3;1 2] [1 1;2 3] [1 1;3 2] [2 1;3 1] [3 1;2 1] [2 3;1 1] [3 2;1 1] [3 1;3 2] [3 2;3 1] [3 3;1 2] [3 3;2 1] [1 3;2 3] [2 3;1 3] [1 2;3 3] [2 1;3 3]                                                   [2 1;2 3] [2 3;2 1] [2 2;1 3] [2 2;3 1]  [1 2;3 2] [3 2;1 2] [1 3;2 2] [3 1;2 2]};
    
    CFAList=SmallCFAList;
    
    %block size
    W1=16;
    W2=96;
    
    if size(im,1)<W2 | size(im,2)<W2
        Result=zeros([size(im,1), size(im,2)]);
        return
    end
    
    
    W2Overlap=round(W2/2);
    
    GChannel=double(im(:,:,2));
    %  GDenoised=DTWDenoise(GChannel,NoiseThresh,Depth);
    % GNoiseResidue=GChannel-GDenoised;
    
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
        
        %ProcIm(:,:,4:6)=(im-double(BilinIm)).^2;
        
        ProcIm=double(ProcIm);
        BlockResult=blockproc(ProcIm,[W1 W1],@eval_block);
        
        Stds=BlockResult(:,:,4:6);
        BlockDiffs=BlockResult(:,:,1:3);
        NonSmooth=Stds>StdThresh;
        
        
        MeanError(TestArray)=mean(mean(mean(BlockDiffs(NonSmooth))));
        BlockDiffs=BlockDiffs./repmat(sum(BlockDiffs,3),[1 1 3]);
    end
    
    [bbb,val]=min(MeanError);
    CFAOut=CFAList{val}==2;
    
    CFAGreen=repmat(CFAOut,round(size(GChannel,1)/2),round(size(GChannel,2)/2));
    ForVarExtraction(:,:,1)=GChannel;
    ForVarExtraction(:,:,2)=CFAGreen;
    
    ForVarExtraction=double(ForVarExtraction);
    F2Map=blockproc(ForVarExtraction,[W2-W2Overlap W2-W2Overlap],@getCFAVar,'BorderSize',[W2Overlap W2Overlap],'PadMethod','symmetric','TrimBorder',0,'UseParallel',1);
    
end

function [ Out ] = eval_block( block_struc )
    %EVAL_BLOCK Summary of this function goes here
    %   Detailed explanation goes here
    im=block_struc.data;
    %Out(:,:,1:3)=mean(mean((double(block_struc.data(:,:,1:3))-double(block_struc.data(:,:,4:6))).^2));
    Out(:,:,1)=mean2((double(block_struc.data(:,:,1))-double(block_struc.data(:,:,4))).^2);
    Out(:,:,2)=mean2((double(block_struc.data(:,:,2))-double(block_struc.data(:,:,5))).^2);
    Out(:,:,3)=mean2((double(block_struc.data(:,:,3))-double(block_struc.data(:,:,6))).^2);
    
    Out(:,:,4)=std(reshape(im(:,:,1),1,numel(im(:,:,1))));
    Out(:,:,5)=std(reshape(im(:,:,2),1,numel(im(:,:,2))));
    Out(:,:,6)=std(reshape(im(:,:,3),1,numel(im(:,:,3))));
end


function [ F2 ] = getCFAVar( block_struc )
    %Wavelet Depth
    Depth=5;
    load('nor_dualtree');
    blockdata=double(block_struc.data);
    G=blockdata(:,:,1);
    GNoise=DTWDenoise(G,Depth,nor);
    G_CFA=boolean(blockdata(:,:,2));
    GOrig=GNoise(G_CFA);
    GInterp=GNoise(~G_CFA);
    var1=var(GOrig);
    var2=var(GInterp);
    F2=max([var1/var2 var2/var1]);
end