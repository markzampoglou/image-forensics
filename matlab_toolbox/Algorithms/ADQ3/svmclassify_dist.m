function [outclass, f] = svmclassify_dist(svmStruct,sample, varargin)
    % This is a modification of the default svmclassify Matlab command. The
    % code has been changed in order to return the distance from the separating
    % hyperplane alongside the class label.
    %
    % Edited by Markos Zampoglou, 2016, ITI-CERTH, Thessaloniki, Greece
    %
    %SVMCLASSIFY Classify data using a support vector machine
    %   GROUP = SVMCLASSIFY(SVMSTRUCT, TEST) classifies each row in TEST using
    %   the support vector machine classifier structure SVMSTRUCT created
    %   using SVMTRAIN, and returns the predicted class level GROUP. TEST must
    %   have the same number of columns as the data used to train the
    %   classifier in SVMTRAIN. GROUP indicates the group to which each row of
    %   TEST is assigned.
    %
    %   GROUP = SVMCLASSIFY(...,'SHOWPLOT',true) plots the test data TEST on
    %   the figure created using the SHOWPLOT option in SVMTRAIN.
    %
    %   Example:
    %       % Load the data and select features for classification
    %       load fisheriris
    %       X = [meas(:,1), meas(:,2)];
    %       % Extract the Setosa class
    %       Y = nominal(ismember(species,'setosa'));
    %       % Randomly partitions observations into a training set and a test
    %       % set using stratified holdout
    %       P = cvpartition(Y,'Holdout',0.20);
    %       % Use a linear support vector machine classifier
    %       svmStruct = svmtrain(X(P.training,:),Y(P.training),'showplot',true);
    %       C = svmclassify(svmStruct,X(P.test,:),'showplot',true);
    %       err_rate = sum(Y(P.test)~= C)/P.TestSize % mis-classification rate
    %       conMat = confusionmat(Y(P.test),C) % the confusion matrix
    %
    %   See also SVMTRAIN, NAIVEBAYES, CLASSREGTREE, CLASSIFY, TREEBAGGER
    
    %   Copyright 2004-2012 The MathWorks, Inc.
    
    
    %   References:
    %
    %     [1] Cristianini, N., Shawe-Taylor, J An Introduction to Support
    %         Vector Machines, Cambridge University Press, Cambridge, UK. 2000.
    %         http://www.support-vector.net
    %     [2] Kecman, V, Learning and Soft Computing,
    %         MIT Press, Cambridge, MA. 2001.
    %     [3] Suykens, J.A.K., Van Gestel, T., De Brabanter, J., De Moor, B.,
    %         Vandewalle, J., Least Squares Support Vector Machines,
    %         World Scientific, Singapore, 2002.
    
    
    % set defaults
    plotflag = false;
    
    % check inputs
    narginchk(2, Inf);
    
    % deal with struct input case
    if ~isstruct(svmStruct)
        error(message('stats:svmclassify:TwoInputsNoStruct'));
    end
    
    if ~isnumeric(sample) || ~ismatrix(sample)
        error(message('stats:svmclassify:BadSample'));
    end
    
    if size(sample,2)~=size(svmStruct.SupportVectors,2)
        error(message('stats:svmclassify:TestSizeMismatch'));
    end
    
    % deal with the various inputs
    if nargin > 2
        if rem(nargin,2) == 1
            error(message('stats:svmclassify:IncorrectNumberOfArguments'));
        end
        okargs = {'showplot','-compilerhelper'};
        for j=1:2:nargin-2
            pname = varargin{j};
            pval = varargin{j+1};
            k = find(strncmpi(pname, okargs,numel(pname)));
            if isempty(k)
                error(message('stats:svmclassify:UnknownParameterName', pname));
            elseif length(k)>1
                error(message('stats:svmclassify:AmbiguousParameterName', pname));
            else
                switch(k)
                    case 1 % plotflag ('SHOWPLOT')
                        plotflag = opttf(pval,okargs{k});
                    case 2 % help the compiler find required function handles by including svmtrain
                        svmtrain(eye(2),[1 0]);
                end
            end
        end
    end
    
    groupnames = svmStruct.GroupNames;
    
    % check group is a vector -- though char input is special...
    if ~isvector(groupnames) && ~ischar(groupnames)
        error(message('stats:svmclassify:GroupNotVector'));
    end
    
    % grp2idx sorts a numeric grouping var ascending, and a string grouping
    % var by order of first occurrence
    [~,groupString,glevels] = grp2idx(groupnames);
    
    % do the classification
    if ~isempty(sample)
        % shift and scale the data if necessary:
        sampleOrig = sample;
        if ~isempty(svmStruct.ScaleData)
            for c = 1:size(sample, 2)
                sample(:,c) = svmStruct.ScaleData.scaleFactor(c) * ...
                    (sample(:,c) +  svmStruct.ScaleData.shift(c));
            end
        end
        
        try
            [outclass,f] = svmdecision(sample,svmStruct);
        catch ME
            error(message('stats:svmclassify:ClassifyFailed', ME.message));
        end
        if plotflag
            
            if isempty(svmStruct.FigureHandles)
                warning(message('stats:svmclassify:NoTrainingFigure'));
                
            else
                try
                    hAxis = svmStruct.FigureHandles{1};
                    hLines = svmStruct.FigureHandles{2};
                    hSV = svmStruct.FigureHandles{3};
                    % unscale the data for plotting purposes
                    [~,hClassLines] = svmplotdata(sampleOrig,outclass,hAxis);
                    trainingString = strcat(cellstr(groupString),' (training)');
                    sampleString = strcat(cellstr(groupString),' (classified)');
                    legend([hLines(1),hClassLines(1),hLines(2),hClassLines(2),hSV],...
                        {trainingString{1},sampleString{1},...
                        trainingString{2},sampleString{2},'Support Vectors'});
                catch ME
                    warning(message('stats:svmclassify:DisplayFailed', ME.message));
                end
            end
        end
        outclass(outclass == -1) = 2;
        unClassified = isnan(outclass);
        outclass = glevels(outclass(~unClassified),:);
        if any(unClassified)
            
            try
                outclass = statinsertnan(unClassified,outclass);
            catch ME
                if ~isequal(ME.identifier,'stats:statinsertnan:LogicalInput')
                    rethrow(ME);
                else
                    error(message('stats:svmclassify:logicalwithNaN'));
                end
            end
        end
        
    else
        outclass = [];
    end
    
