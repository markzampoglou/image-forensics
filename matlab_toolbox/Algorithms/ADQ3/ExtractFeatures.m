function Feature=ExtractFeatures(im, c1, c2, ncomp, digitBinsToKeep)
    % This function extracts a descriptor feature based on the first-digit
    % distribution of DCT coefficients of an image. It is needed by
    % BenfordDQ.m 
    %
    % c1 and c2 are the first and last DCT coefficients to be taken into
    % account, DC term included (default: c1=2, c2=10).
    % ncomp is the component from which to extract the feature (default: 1,
    % which corresponds to the Y component
    % digitBinsToKeep is an array containing the digits for which we keep
    % their frequency. Default digitBinsToKeep=[2 5 7];
    %
    % Copyright (C) 2016 Markos Zampoglou
    % Information Technologies Institute, Centre for Research and Technology Hellas
    % 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece
    
    coeffArray = im.coef_arrays{ncomp};
    qtable = im.quant_tables{im.comp_info(ncomp).quant_tbl_no};
    Y = dequantize(coeffArray, qtable);
    
    coeff = [1 9 2 3 10 17 25 18 11 4 5 12 19 26 33 41 34 27 20 13 6 7 14 21 28 35 42 49 57 50 43 36 29 22 15 8 16 23 30 37 44 51 58 59 52 45 38 31 24 32 39 46 53 60 61 54 47 40 48 55 62 63 56 64];
    sizeCA = size(coeffArray);
    
    for index = c1:c2
        coeffFreq = zeros(1, numel(coeffArray)/64);
        coe = coeff(index);
        % load DCT coefficients at position index
        k = 1;
        start = mod(coe,8);
        if start == 0
            start = 8;
        end
        for l = start:8:sizeCA(2)
            for i = ceil(coe/8):8:sizeCA(1)
                coeffFreq(k) = Y(i,l);
                k = k+1;
            end
        end
        
        NumOfDigits=floor(log10(abs(coeffFreq)+0.5)) + 1;
        FirstDigit=floor(abs(coeffFreq)./(10.^(NumOfDigits-1)));
        
        % get histogram of DCT coefficients' first digits
        binHist = (0:9);
        digitHist(index-c1+1,:) = hist(FirstDigit,binHist);
    end
    
    
    HistToKeep=digitHist(:,digitBinsToKeep+1);
    Feature=HistToKeep;
    Feature=reshape(HistToKeep',1,numel(HistToKeep));
    
    
