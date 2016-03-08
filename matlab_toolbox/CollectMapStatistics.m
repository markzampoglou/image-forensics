function Curves = CollectMapStatistics( Options )
    DatasetName=Options.DatasetName;
    AlgorithmName=Options.AlgorithmName;
    
    MasksPath=Options.MasksPath;
    SplicedOutputPath=[Options.OutputPath DatasetName AlgorithmName filesep 'Sp' filesep];
    AuthenticOutputPath=[Options.OutputPath DatasetName AlgorithmName filesep 'Au' filesep];
    EvalOutputPath=[Options.OutputPath 'Evals' filesep];
    OutputFilesSp=getAllFiles(SplicedOutputPath,'*.mat',true);
    OutputFilesAu=getAllFiles(AuthenticOutputPath,'*.mat',true);
    
    warning('off','MATLAB:MKDIR:DirectoryExists');
    mkdir(EvalOutputPath);
    warning('on','all');
    
    for FileInd=1:length(OutputFilesSp);
        LoadedOutput=load(OutputFilesSp{FileInd});
        %Our existing outputs do not contain masks. Remove prior to
        %publication. ***********************************************
        [~,InputName,~]=fileparts(strrep(OutputFilesSp{FileInd},'.mat',''));
        BinMaskPath=dir([MasksPath InputName '.*']);
        if ~isempty(BinMaskPath)
            BinMask=mean(double(imread([MasksPath BinMaskPath.name])),3)>128;
        else
            BinMaskPath=dir([MasksPath '*.png']);
            if length(BinMaskPath)>1
                error('Something is wrong with the masks');
            else
                BinMask=mean(double(imread([MasksPath BinMaskPath(1).name])),3)>128;
            end
        end
        LoadedOutput.BinMask=BinMask;
        % End of additional code
        % ***********************************************************
        
        ResultsSp(FileInd)=OutputFileStatistics(LoadedOutput);
    end
    save([EvalOutputPath AlgorithmName '_' DatasetName 'Sp.mat'],'ResultsSp');
    
    for FileInd=1:length(OutputFilesAu);
        LoadedOutput=load(OutputFilesAu{FileInd});
        ResultsAu(FileInd)=OutputFileStatistics(LoadedOutput);
    end
    save([EvalOutputPath AlgorithmName '_' DatasetName 'Au.mat'],'ResultsAu');
    
    %Get the value ranges for the metrics
    MeansDiffsAu=cell2mat({ResultsAu.MaskMean})-cell2mat({ResultsAu.OutsideMean});
    MeansDiffsSp=cell2mat({ResultsSp.MaskMean})-cell2mat({ResultsSp.OutsideMean});
    MediansDiffsAu=cell2mat({ResultsAu.MaskMedian})-cell2mat({ResultsAu.OutsideMedian});
    MediansDiffsSp=cell2mat({ResultsSp.MaskMedian})-cell2mat({ResultsSp.OutsideMedian});
    
    MeansDiffs=abs([MeansDiffsSp MeansDiffsAu]);
    MediansDiffs=abs([MediansDiffsSp MediansDiffsAu]);
    MeansRange=[prctile(MeansDiffs,2) prctile(MeansDiffs,98)];
    MediansRange=[prctile(MediansDiffs,2) prctile(MediansDiffs,98)];
    
    MeanStep=(MeansRange(2)-MeansRange(1))/500;
    MedianStep=(MediansRange(2)-MediansRange(1))/500;
    MeanThreshValues=[-inf MeansRange(1):MeanStep:MeansRange(2) inf];
    MedianThreshValues=[-inf MediansRange(1):MedianStep:MediansRange(2) inf];
    
    if length(MeanThreshValues)~=length(MedianThreshValues)
        error('something is wrong in the thresholding procedure');
    end
    
    for ThreshInd=1:length(MeanThreshValues);
        MeanThresh=MeanThreshValues(ThreshInd);
        MedianThresh=MedianThreshValues(ThreshInd);
        %Top row is authentic, bottom row is spliced
        Curves.MedianPositives(1,ThreshInd)=mean(MeansDiffsAu>=MedianThresh);
        Curves.MedianPositives(2,ThreshInd)=mean(MeansDiffsSp>=MedianThresh);
        Curves.MeanPositives(1,ThreshInd)=mean(MediansDiffsAu>=MeanThresh);
        Curves.MeanPositives(2,ThreshInd)=mean(MediansDiffsSp>=MeanThresh);
    end
    
    Curves.MeanThreshValues=MeanThreshValues;
    Curves.MedianThreshValues=MedianThreshValues;
    
    KSThreshValues=0:1/800:1;
    KSListAu={ResultsAu.KSStat};
    KSListAu(cellfun(@isempty,KSListAu))={repmat(0,[1 length(KSListAu(cellfun(@isempty,KSListAu)))])};
    KSListAu=cell2mat(KSListAu);
    KSListSp={ResultsSp.KSStat};
    KSListSp(cellfun(@isempty,KSListSp))={repmat(0,[1 length(KSListSp(cellfun(@isempty,KSListSp)))])};
    KSListSp=cell2mat(KSListSp);
    
    for ThreshInd=1:801
        Thresh=KSThreshValues(ThreshInd);
        Curves.KSPositives(1,ThreshInd)=mean(KSListAu>=Thresh);
        Curves.KSPositives(2,ThreshInd)=mean(KSListSp>=Thresh);
    end
    
end

