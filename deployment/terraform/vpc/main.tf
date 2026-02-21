module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = ">= 4.0.0"

  name = "${var.cluster_name}-vpc"
  cidr = var.vpc_cidr

  azs             = slice(data.aws_availability_zones.available.names, 0, 3)
  public_subnets  = length(var.public_subnets) > 0 ? var.public_subnets : [
    cidrsubnet(var.vpc_cidr, 8, 0),
    cidrsubnet(var.vpc_cidr, 8, 1),
    cidrsubnet(var.vpc_cidr, 8, 2),
  ]
  private_subnets = length(var.private_subnets) > 0 ? var.private_subnets : [
    cidrsubnet(var.vpc_cidr, 8, 100),
    cidrsubnet(var.vpc_cidr, 8, 101),
    cidrsubnet(var.vpc_cidr, 8, 102),
  ]

  enable_dns_hostnames = true
  enable_dns_support   = true
  enable_nat_gateway   = true
  single_nat_gateway   = true

  tags = merge(var.tags, {
    "Name" = "${var.cluster_name}-vpc"
  })
}

data "aws_availability_zones" "available" {}
