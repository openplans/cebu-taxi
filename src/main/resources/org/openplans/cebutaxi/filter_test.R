library(Matrix)
library(dlm)
library(lattice)
library(latticeExtra)

proj_data = ts(read.csv("test_data/proj_output.csv")[,2:3])
colnames(proj_data)

# in minutes
timeDiff = 1                                                          
aVariance = 25
gVariance = 50

m0 = c(proj_data[1, 1], 0, proj_data[1,2], 0)
Ct = diag(rep(1,4)) * aVariance

# Classic kinematics
F = diag(c(1,1))
F[1,2] = timeDiff
Ft = diag(2) %x% F
# Constant Turn-rate model
#turnRate = 0.01
#Ft = cbind(c(1,0,0,0), 
#           c(timeDiff, 1-(turnRate*timeDiff)^2/2, 
#             turnRate*timeDiff^2/2, turnRate*timeDiff), 
#           c(0,0,1,0), 
#           c(-turnRate*timeDiff^2/2, -turnRate*timeDiff,
#             timeDiff, 1-(turnRate*timeDiff)^2/2))

G = c(timeDiff^2/2, timeDiff)
GG = diag(2) %x% G
QQtmp = drop(GG %*% diag(c(aVariance,aVariance))) %*% t(GG)
if (all(eigen(QQtmp, symmetric=T, only.values=T)$values >= 0)) {
  QQ = QQtmp
} else {
  QQ = as.matrix(nearPD(QQtmp)$mat)
}

O = t(diag(2) %x% c(1,0))

ObsM = rbind(O, O%*%Ft)
sum(svd(ObsM)$d > 0)

filter = dlm(m0 = m0, C0 = Ct,
             FF = O, V = gVariance*diag(c(1,1)),
             GG = Ft, W = QQ)

data_window = window(proj_data, end=400)
res = dlmFilter(data_window, filter)
str(res, max.level=1)
str(res$f, max.level=1)


sdev = residuals(res)$sd
lwr = res$f + qnorm(0.025)*sdev
upr = res$f - qnorm(0.025)*sdev
p1 = xyplot(cbind(data_window,res$f, lwr, upr), type='n', 
            screens=c(1,2,1,2,1,2,1,2))
p1 = p1 + xyplot(data_window[,1], type='l', col="black", screens=1)
p1 = p1 + xyplot(data_window[,2], type='l', col="black", screens=2)

p1 = p1 + xyplot(lwr[,1], type='l', col="red", lty=2, screens=1)
p1 = p1 + xyplot(res$f[,1], type='l', col="red", screens=1)
p1 = p1 + xyplot(upr[,1], type='l', col="red", lty=2, screens=1)

p1 = p1 + xyplot(lwr[,2], type='l', col="red", lty=2, screens=2)
p1 = p1 + xyplot(res$f[,2], type='l', col="red", screens=2)
p1 = p1 + xyplot(upr[,2], type='l', col="red", lty=2, screens=2)
plot(p1)

