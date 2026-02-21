terraform { 
  required_version = ">= 1.0.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 4.0"
    }
  }

  cloud { 
    organization = "habitquest" 
    workspaces { 
      name = "habitquest-cli" 
    } 
  } 
}

provider "aws" {
  region  = var.aws_region

  assume_role {
    role_arn = var.role_arn
    # role_arn = "arn:aws:iam::471112759337:role/terraform-manual-role"
    # role_arn = "arn:aws:iam::471112759337:role/github-actions-terraform-role"
  }
}

module "vpc" {
  source = "./vpc"

  vpc_cidr      = var.vpc_cidr_block
  public_subnets = var.public_subnet_cidrs
  private_subnets = var.private_subnet_cidrs
  tags = var.tags
}

module "eks" {
  source  = "./eks"

  name    = var.cluster_name
  cluster_version = var.cluster_version
  node_groups     = var.node_groups
  tags            = var.tags

  vpc_id  = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets
}